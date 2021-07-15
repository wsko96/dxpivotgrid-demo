package kr.wise.demo.pivotgrid.util;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.node.ArrayNode;

import kr.wise.demo.pivotgrid.param.FilterParam;
import kr.wise.demo.pivotgrid.param.GroupParam;
import kr.wise.demo.pivotgrid.param.GroupSummaryParam;

public final class ParamUtils {

    private ParamUtils() {

    }

    public static FilterParam[] toFilterParams(final ArrayNode filterParamsNode) {
        if (filterParamsNode == null) {
            return null;
        }

        final int size = filterParamsNode != null ? filterParamsNode.size() : 0;
        FilterParam[] params = new FilterParam[size];

        for (int i = 0; i < size; i++) {
            params[i] = toFilterParam((ArrayNode) filterParamsNode.get(i));
        }

        return params;
    }

    public static FilterParam toFilterParam(final ArrayNode filterParamNode) {
        final int size = filterParamNode.size();
        final String selector = size > 0 ? filterParamNode.get(0).asText() : null;
        final String operator = size > 1 ? filterParamNode.get(1).asText() : null;
        final String comparingValue = size > 2 ? filterParamNode.get(2).asText() : null;
        return new FilterParam(selector, operator, comparingValue);
    }

    public static GroupParam[] toGroupParams(final ArrayNode groupParamsNode) {
        if (groupParamsNode == null) {
            return null;
        }

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
