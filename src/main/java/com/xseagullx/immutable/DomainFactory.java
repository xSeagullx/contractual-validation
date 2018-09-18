package com.xseagullx.immutable;

import lombok.extern.slf4j.Slf4j;
import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.Advisor;
import org.springframework.aop.MethodBeforeAdvice;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.JdkRegexpMethodPointcut;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

@Slf4j
public class DomainFactory {
	private final Advice readOnlyAdvice = (MethodBeforeAdvice) (method, args, target) -> {
		log.info("about to call method " + method + " on immutable object. Stop");
		throw new DomainIsReadOnlyException(method.getName());
	};

	public <T> T toImmutable(T unwrappedDomain) {
		ProxyFactory factory = new ProxyFactory(unwrappedDomain);
		factory.setProxyTargetClass(true);

		factory.addAdvisors(getReadOnlyAdvisor(), getWithWritableAdvisor(unwrappedDomain));
		//noinspection unchecked
		return (T) factory.getProxy();
	}

	private <T> Advisor getWithWritableAdvisor(T unwrappedDomain) {
		HashSet<String> touchedProperties = new HashSet<>();

		ProxyFactory factory = new ProxyFactory(unwrappedDomain);
		factory.addInterface(Validating.class);
		factory.setProxyTargetClass(true);
		factory.addAdvisors(getTrackingAdvice(touchedProperties), getTouchedPropertiesAdvice(touchedProperties));

		JdkRegexpMethodPointcut pointcut = new JdkRegexpMethodPointcut();
		pointcut.setPattern(".*withWritable.*");
		return new DefaultPointcutAdvisor(pointcut, (MethodInterceptor) invocation -> {
			//noinspection unchecked
			((Consumer)invocation.getArguments()[0]).accept(factory.getProxy());
			return invocation.getThis();
		});
	}

	private DefaultPointcutAdvisor getReadOnlyAdvisor() {
		JdkRegexpMethodPointcut pointcut = new JdkRegexpMethodPointcut();
		pointcut.setPattern(".*set.*");
		return new DefaultPointcutAdvisor(pointcut, readOnlyAdvice);
	}

	private DefaultPointcutAdvisor getTrackingAdvice(Set<String> touchedProperties) {
		JdkRegexpMethodPointcut pointcut = new JdkRegexpMethodPointcut();
		pointcut.setPattern(".*set.*");
		return new DefaultPointcutAdvisor(pointcut, (MethodBeforeAdvice) (method, args, target) -> touchedProperties.add(method.getName()));
	}

	private DefaultPointcutAdvisor getTouchedPropertiesAdvice(Set<String> touchedProperties) {
		JdkRegexpMethodPointcut pointcut = new JdkRegexpMethodPointcut();
		pointcut.setPattern(".*getTouchedProperties()");
		return new DefaultPointcutAdvisor(pointcut, (MethodInterceptor) invocation -> touchedProperties);
	}
}
