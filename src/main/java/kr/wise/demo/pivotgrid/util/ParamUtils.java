package kr.wise.demo.pivotgrid.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.node.ArrayNode;

import kr.wise.demo.pivotgrid.param.FilterParam;
import kr.wise.demo.pivotgrid.param.GroupParam;
import kr.wise.demo.pivotgrid.param.GroupSummaryParam;

public final class ParamUtils {

    private static Logger log = LoggerFactory.getLogger(ParamUtils.class);

    private ParamUtils() {

    }

    public static FilterParam toFilterParam(final ArrayNode filterParamsNode) {
        final int size = filterParamsNode != null ? filterParamsNode.size() : 0;
        final String operator = size > 1 ? filterParamsNode.get(1).asText() : null;
        final FilterParam rootFilter;

        if ("and".equals(operator) || "or".equals(operator)) {
            rootFilter = new FilterParam(operator);
            final ArrayNode firstFilterNodeWrapper = (ArrayNode) filterParamsNode.get(0);
            final ArrayNode secondFilterNodeWrapper = (ArrayNode) filterParamsNode.get(2);
            addChildFilterParam(rootFilter, (ArrayNode) firstFilterNodeWrapper.get(0));
            addChildFilterParam(rootFilter, (ArrayNode) secondFilterNodeWrapper.get(0));
        } else {
            rootFilter = new FilterParam();

            for (int i = 0; i < size; i++) {
                addChildFilterParam(rootFilter, (ArrayNode) filterParamsNode.get(i));
            }
        }

        return rootFilter;
    }

    private static void addChildFilterParam(final FilterParam filterParam, final ArrayNode childFilterParamNode) {
        final int size = childFilterParamNode != null ? childFilterParamNode.size() : 0;
        final String operator = size > 1 ? childFilterParamNode.get(1).asText() : null;

        if (StringUtils.isBlank(operator)) {
            return;
        }

        if ("and".equals(operator) || "or".equals(operator)) {
            FilterParam childFilter = new FilterParam(operator);
            addChildFilterParam(childFilter, (ArrayNode) childFilterParamNode.get(0));
            addChildFilterParam(childFilter, (ArrayNode) childFilterParamNode.get(2));
        } else {
            final String selector = childFilterParamNode.get(0).asText();
            final String comparingValue = childFilterParamNode.get(2).asText();
            filterParam.addChild(operator, selector, comparingValue);
        }
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
