package org.springframework.boot.actuate.endpoint2.jmx;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.ReflectionException;


import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.actuate.endpoint.jmx.DataConverter;
import org.springframework.boot.actuate.endpoint2.EndpointInfo;
import org.springframework.boot.actuate.endpoint2.EndpointOperationInfo;
import org.springframework.util.ReflectionUtils;

/**
 * A {@link DynamicMBean} that invokes an {@link EndpointInfo endpoint}. Convert known
 * input parameters such as enums automatically.
 *
 * @author Stephane Nicoll
 * @since 2.0.0
 * @see EndpointMBeanInfoAssembler
 */
public class EndpointDynamicMBean implements DynamicMBean {

	private final BeanFactory beanFactory;

	private final DataConverter dataConverter;

	private final EndpointMBeanInfo endpointInfo;

	public EndpointDynamicMBean(BeanFactory beanFactory, DataConverter dataConverter,
			EndpointMBeanInfo endpointInfo) {
		this.beanFactory = beanFactory;
		this.dataConverter = dataConverter;
		this.endpointInfo = endpointInfo;
	}

	@Override
	public MBeanInfo getMBeanInfo() {
		return this.endpointInfo.getMbeanInfo();
	}

	@Override
	public Object invoke(String actionName, Object[] params, String[] signature) throws MBeanException, ReflectionException {
		EndpointOperationInfo operationInfo = this.endpointInfo.getOperations()
				.get(actionName);
		if (operationInfo != null) {
			Object bean = this.beanFactory.getBean(operationInfo.getBeanName());
			Method method = operationInfo.getOperationMethod();
			Object result = ReflectionUtils.invokeMethod(method, bean,
					convertParameters(params, method.getParameters()));
			return dataConverter.convert(result);
		}
		return null; // TODO?
	}

	private Object[] convertParameters(Object[] inbound, Parameter[] parameters) {
		Object[] mapped = new Object[inbound.length];
		for (int i = 0; i < inbound.length; i++) {
			mapped[i]= convertParameter(inbound[i], parameters[i]);
		}
		return mapped;
	}

	@SuppressWarnings("unchecked")
	private Object convertParameter(Object inbound, Parameter parameter) {
		if (inbound == null) {
			return null;
		}
		if (parameter.getType().isEnum() && inbound instanceof String) {
			return Enum.valueOf((Class<? extends Enum>) parameter.getType(),
					String.valueOf(inbound).toUpperCase());
		}
		return inbound;
	}

	@Override
	public Object getAttribute(String attribute) throws AttributeNotFoundException, MBeanException, ReflectionException {
		throw new AttributeNotFoundException();
	}

	@Override
	public void setAttribute(Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
		throw new AttributeNotFoundException();
	}

	@Override
	public AttributeList getAttributes(String[] attributes) {
		return new AttributeList();
	}

	@Override
	public AttributeList setAttributes(AttributeList attributes) {
		return new AttributeList();
	}

}
