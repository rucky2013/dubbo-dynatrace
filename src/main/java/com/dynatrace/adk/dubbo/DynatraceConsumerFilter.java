package com.dynatrace.adk.dubbo;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.support.RpcUtils;
import com.dynatrace.adk.DynaTraceADKFactory;
import com.dynatrace.adk.Tagging;

@Activate(group = Constants.CONSUMER, order = Integer.MAX_VALUE)
public class DynatraceConsumerFilter implements Filter {

	private static final String DYNATRACE_TAG_KEY = "DYNATRACE_TAG_KEY";

	public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {

		try {
			// initialize the dynaTrace ADK
			DynaTraceADKFactory.initialize();

			// get an instance of the Tagging ADK
			Tagging tagging = DynaTraceADKFactory.createTagging();

			String tagString = tagging.getTagAsString();

			boolean isAsync = RpcUtils.isAsync(invoker.getUrl(), invocation);

			tagging.linkClientPurePath(isAsync, tagString);

			invocation.getAttachments().put(DYNATRACE_TAG_KEY, tagString);
		} catch (Throwable t) {
			// do nothing
		}

		Result result = invoker.invoke(invocation);

		try {
			DynaTraceADKFactory.uninitialize();
		} catch (Throwable t) {
			// do nothing
		}

		return result;
	}

}
