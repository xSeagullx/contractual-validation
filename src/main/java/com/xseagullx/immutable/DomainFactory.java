package com.xseagullx.immutable;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.Advisor;
import org.springframework.aop.ProxyMethodInvocation;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.JdkRegexpMethodPointcut;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.ConfigurablePropertyAccessor;
import org.springframework.beans.NotReadablePropertyException;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.util.ClassUtils;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

@Getter
@RequiredArgsConstructor
class DomainState<T, E> {
	private final Contract<T, E> contract;
	private final ErrorHandlingStrategy<E> errorHandlingStrategy;
	private final Set<String> touchedProperties = new HashSet<>();
	@Setter private T domainRoot;

	@Setter private boolean closed = true;
}

@Slf4j
public class DomainFactory {
	public <T> T toImmutable(T unwrappedDomain) {
		return toImmutable(unwrappedDomain, new Contract<>(Collections.emptyList()), validationResult -> {
			if (validationResult != null) {
				throw new RuntimeException("Failed validation " + validationResult);
			}
		});
	}

	public <T, E> T toImmutable(T unwrappedDomain, Contract<T, E> contract, ErrorHandlingStrategy<E> errorHandlingStrategy) {
		return toImmutable(unwrappedDomain, new DomainState<>(contract, errorHandlingStrategy));
	}

	private <T, E> T toImmutable(T unwrappedDomain, DomainState<T, E> domainState) {
		//noinspection unchecked
		T proxy = (T) proxyNestedFields(unwrappedDomain, domainState, null);
		domainState.setDomainRoot(proxy);
		return proxy;
	}

	private <T, E> Object proxyNestedFields(Object unwrappedDomain, DomainState<T, E> domainState, String path) {
		BeanWrapper beanWrapper = PropertyAccessorFactory.forBeanPropertyAccess(unwrappedDomain);
		ConfigurablePropertyAccessor directFieldAccess = PropertyAccessorFactory.forDirectFieldAccess(unwrappedDomain);
		for (PropertyDescriptor property : beanWrapper.getPropertyDescriptors()) {
			Object propertyValue = getValue(beanWrapper, directFieldAccess, property.getName());
			if (shouldProxy(propertyValue)) {
				Object proxy = proxyNestedFields(propertyValue, domainState, (path == null ? "" : path + ".") + property.getName());
				directFieldAccess.setPropertyValue(property.getName(), proxy);
			}
		}
		return makeImmutable(unwrappedDomain, domainState, path);
	}

	private Object getValue(BeanWrapper propertyAccessor, ConfigurablePropertyAccessor fieldAccessor, String name) {
		try {
			return propertyAccessor.getPropertyValue(name);
		}
		catch (NotReadablePropertyException ignored) {
			return fieldAccessor.getPropertyValue(name);
		}
		catch (Exception ignored) {
			log.warn("Can't access getter for {} {}. Do you have getters with side effects?", propertyAccessor.getWrappedClass(), name);
			return fieldAccessor.getPropertyValue(name);
		}
	}

	private Object makeImmutable(Object object, DomainState domainState, String path) {
		ProxyFactory factory = new ProxyFactory(object);
		factory.setProxyTargetClass(true);
		factory.addInterface(Immutable.class);
		factory.addInterface(Validating.class);

		factory.addAdvisors(getSetterControlAdvisor(domainState, path));
		factory.addAdvisors(getTouchedPropertiesAdvice(domainState));
		if (object instanceof HasWithWritable)
			factory.addAdvisor(getWithWritableAdvisor(domainState));
		return factory.getProxy();
	}

	private Advisor getSetterControlAdvisor(DomainState domainState, String path) {
		JdkRegexpMethodPointcut pointcut = new JdkRegexpMethodPointcut();
		pointcut.setPattern(".*set.*");
		return new DefaultPointcutAdvisor(pointcut, (MethodInterceptor) invocation -> {
			String propertyPath = (path == null ? "" : path + ".") + getPropertyName(invocation.getMethod());
			if (domainState.isClosed()) {
				log.info("about to call method " + invocation.getMethod() + " on immutable object. Stop");
				throw new DomainIsReadOnlyException(propertyPath);
			} else {
				// If object we are adding is not proxied - proxy it.
				Object valueToSet = invocation.getArguments()[0];
				if (shouldProxy(valueToSet)) {
					invocation.getArguments()[0] = makeImmutable(valueToSet, domainState, propertyPath);
				}
				domainState.getTouchedProperties().add(propertyPath);
				return invocation.proceed();
			}
		});
	}

	private Advisor getWithWritableAdvisor(DomainState domainState) {
		JdkRegexpMethodPointcut pointcut = new JdkRegexpMethodPointcut();
		pointcut.setPattern(".*withWritable.*");
		return new DefaultPointcutAdvisor(pointcut, (MethodInterceptor) invocation -> {
			if (!(invocation instanceof ProxyMethodInvocation))
				throw new IllegalStateException("This interceptor should be only called with ProxyMethodInvocation");

			ProxyMethodInvocation proxyMethodInvocation = (ProxyMethodInvocation) invocation;
			domainState.setClosed(false); // Open object for modifications

			//noinspection unchecked
			((Consumer)invocation.getArguments()[0]).accept(proxyMethodInvocation.getProxy());

			// Make Domain immutable again
			domainState.setClosed(true);
			domainState.getContract().enforce(domainState);
			domainState.getTouchedProperties().clear();
			return proxyMethodInvocation.getProxy();
		});
	}

	private DefaultPointcutAdvisor getTouchedPropertiesAdvice(DomainState domainState) {
		JdkRegexpMethodPointcut pointcut = new JdkRegexpMethodPointcut();
		pointcut.setPattern(".*getTouchedProperties()");
		return new DefaultPointcutAdvisor(pointcut, (MethodInterceptor) invocation -> domainState.getTouchedProperties());
	}

	// TODO extract to a strategy class
	private boolean shouldProxy(Object valueToSet) {
		if (valueToSet == null)
			return false;
		Class<?> clazz = valueToSet.getClass();
		return !ClassUtils.isPrimitiveOrWrapper(clazz)
				&& !BeanUtils.isSimpleValueType(clazz)
				&& !Modifier.isFinal(clazz.getModifiers());
	}

	private String getPropertyName(Method method) {
		return Introspector.decapitalize(method.getName().substring(3));
	}
}
