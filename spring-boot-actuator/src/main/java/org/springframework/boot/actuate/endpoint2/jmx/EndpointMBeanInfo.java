package org.springframework.boot.actuate.endpoint2.jmx;

import java.util.Map;


import javax.management.MBeanInfo;

import org.springframework.boot.actuate.endpoint2.EndpointInfo;
import org.springframework.boot.actuate.endpoint2.EndpointOperationInfo;

/**
 * The {@link MBeanInfo} for a particular {@link EndpointInfo endpoint}. Maps operation
 * names to a {@link EndpointOperationInfo}.
 *
 * @author Stephane Nicoll
 * @since 2.0.0
 */
public final class EndpointMBeanInfo {

	private final MBeanInfo mBeanInfo;

	private final Map<String, EndpointOperationInfo> operations;

	public EndpointMBeanInfo(MBeanInfo mBeanInfo,
			Map<String, EndpointOperationInfo> operations) {
		this.mBeanInfo = mBeanInfo;
		this.operations = operations;
	}

	public MBeanInfo getMbeanInfo() {
		return this.mBeanInfo;
	}

	public Map<String, EndpointOperationInfo> getOperations() {
		return this.operations;
	}

}
