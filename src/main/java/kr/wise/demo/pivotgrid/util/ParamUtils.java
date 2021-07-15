package kr.wise.demo.pivotgrid.util;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.node.ArrayNode;

import kr.wise.demo.pivotgrid.param.GroupParam;
import kr.wise.demo.pivotgrid.param.GroupSummaryParam;

public final class ParamUtils {

    private ParamUtils() {

    }

    public static GroupParam[] toGroupParams(final ArrayNode groupParamsNode) {
        final List<GroupParam> params = new ArrayList<>();
        final int size = groupParamsNode != null ? groupParamsNode.size() : 0;

        for (int i = 0; i < size; i++) {
            params.add(toGroupParam(groupParamsNode.get(i)));
        }

        return params.toArray(new GroupParam[params.size()]);
    }

    public static GroupParam toGroupParam(final Object groupParamNode) {
        return JacksonUtils.getObjectMapper().convertValue(groupParamNode, GroupParam.class);
    }

    public static GroupSummaryParam[] toGroupSummaryParams(final ArrayNode groupSummaryParamsNode) {
        final List<GroupSummaryParam> params = new ArrayList<>();
        final int size = groupSummaryParamsNode != null ? groupSummaryParamsNode.size() : 0;

        for (int i = 0; i < size; i++) {
            params.add(toGroupSummaryParam(groupSummaryParamsNode.get(i)));
        }

        return params.toArray(new GroupSummaryParam[params.size()]);
    }

    public static GroupSummaryParam toGroupSummaryParam(final Object groupSummaryParamNode) {
        return JacksonUtils.getObjectMapper().convertValue(groupSummaryParamNode,
                GroupSummaryParam.class);
    }

}
