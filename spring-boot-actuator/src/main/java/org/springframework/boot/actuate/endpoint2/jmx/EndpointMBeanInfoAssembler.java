package org.springframework.boot.actuate.endpoint2.jmx;

import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.modelmbean.ModelMBeanAttributeInfo;
import javax.management.modelmbean.ModelMBeanConstructorInfo;
import javax.management.modelmbean.ModelMBeanInfoSupport;
import javax.management.modelmbean.ModelMBeanNotificationInfo;
import javax.management.modelmbean.ModelMBeanOperationInfo;

import org.springframework.boot.actuate.endpoint2.EndpointInfo;
import org.springframework.boot.actuate.endpoint2.EndpointOperationInfo;
import org.springframework.boot.actuate.endpoint2.EndpointOperationType;

/**
 * Gather the management operations of a particular {@link EndpointInfo endpoint}.
 *
 * @author Stephane Nicoll
 * @since 2.0.0
 */
public class EndpointMBeanInfoAssembler {

	/**
	 * Provide the {@link EndpointMBeanInfo} for the specified
	 * {@link EndpointInfo endpoint}.
	 * @param endpointInfo the endpoint to handle
	 * @return the mbean info for the endpoint
	 */
	public EndpointMBeanInfo getEndpointMBeanInfo(EndpointInfo endpointInfo) {
		Map<String, OperationInfos> operationsMapping = getOperationInfo(endpointInfo);
		ModelMBeanOperationInfo[] operationsMBeanInfo = operationsMapping.values()
				.stream().map(t -> t.mBeanOperationInfo)
				.collect(Collectors.toList()).toArray(new ModelMBeanOperationInfo[] {});
		Map<String, EndpointOperationInfo> operationsInfo = new LinkedHashMap<>();
		operationsMapping.forEach((name, t) -> operationsInfo.put(name, t.operationInfo));

		MBeanInfo info = new ModelMBeanInfoSupport(
				getClassName(endpointInfo), getDescription(endpointInfo),
				new ModelMBeanAttributeInfo[0], new ModelMBeanConstructorInfo[0],
				operationsMBeanInfo, new ModelMBeanNotificationInfo[0]);
		return new EndpointMBeanInfo(info, operationsInfo);
	}

	private String getClassName(EndpointInfo endpointInfo) {
		return endpointInfo.getId();
	}

	private String getDescription(EndpointInfo endpointInfo) {
		return "MBean operations for endpoint " + endpointInfo.getId();
	}

	private Map<String, OperationInfos> getOperationInfo(EndpointInfo<?> endpointInfo) {
		Map<String, OperationInfos> operationInfos = new HashMap<>();
		endpointInfo.getOperations().forEach((type, operationInfo) -> {
			// TODO two methods with the same name may exist
			String name = operationInfo.getOperationMethod().getName();
			ModelMBeanOperationInfo mBeanOperationInfo = new ModelMBeanOperationInfo(name,
					"Invoke " + name + " for endpoint " + endpointInfo.getId(),
					getMBeanParameterInfos(operationInfo),
					mapParameterType(operationInfo.getOperationMethod().getReturnType()),
					mapOperationType(type));
			operationInfos.put(name, new OperationInfos(mBeanOperationInfo, operationInfo));
		});
		return operationInfos;
	}

	private MBeanParameterInfo[] getMBeanParameterInfos(EndpointOperationInfo info) {
		List<MBeanParameterInfo> parameters = new ArrayList<>();
		for (Parameter parameter : info.getOperationMethod().getParameters()) {
			parameters.add(new MBeanParameterInfo(parameter.getName(),
					mapParameterType(parameter.getType()), null));
		}
		return parameters.toArray(new MBeanParameterInfo[0]);
	}

	private String mapParameterType(Class<?> parameter) {
		if (parameter.isEnum()) {
			return String.class.getName();
		}
		if (parameter.equals(Void.TYPE)) {
			return parameter.getName();
		}
		if (!parameter.getName().startsWith("java.")) {
			return Object.class.getName();
		}
		return parameter.getName();
	}

	private int mapOperationType(EndpointOperationType type) {
		if (type == EndpointOperationType.READ
				|| type == EndpointOperationType.PARTIAL_READ) {
			return MBeanOperationInfo.INFO;
		}
		if (type == EndpointOperationType.WRITE) {
			return MBeanOperationInfo.ACTION;
		}
		return MBeanOperationInfo.UNKNOWN;
	}

	private static class OperationInfos {
		private final ModelMBeanOperationInfo mBeanOperationInfo;

		private final EndpointOperationInfo operationInfo;

		OperationInfos(ModelMBeanOperationInfo mBeanOperationInfo,
				EndpointOperationInfo operationInfo) {
			this.mBeanOperationInfo = mBeanOperationInfo;
			this.operationInfo = operationInfo;
		}
	}

}
