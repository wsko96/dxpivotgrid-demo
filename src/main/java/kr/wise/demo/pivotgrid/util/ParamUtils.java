package kr.wise.demo.pivotgrid.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import kr.wise.demo.pivotgrid.param.FilterParam;
import kr.wise.demo.pivotgrid.param.GroupParam;
import kr.wise.demo.pivotgrid.param.SummaryParam;

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
            addChildFilterParam(rootFilter, unwrapDoubleArrayNode(firstFilterNodeWrapper));
            addChildFilterParam(rootFilter, unwrapDoubleArrayNode(secondFilterNodeWrapper));
        } else {
            rootFilter = new FilterParam();

            for (int i = 0; i < size; i++) {
                addChildFilterParam(rootFilter, (ArrayNode) filterParamsNode.get(i));
            }
        }

        return rootFilter;
    }

    private static ArrayNode unwrapDoubleArrayNode(final ArrayNode arrayNode) {
        final int size = arrayNode.size();
        if (size == 1) {
            final JsonNode innerNode = arrayNode.get(0);
            if (innerNode.isArray()) {
                return (ArrayNode) innerNode;
            }
        }
        return arrayNode;
    }

    private static void addChildFilterParam(final FilterParam filterParam, final ArrayNode childFilterParamNode) {
        final int size = childFilterParamNode != null ? childFilterParamNode.size() : 0;
        final String operator = size > 1 ? childFilterParamNode.get(1).asText() : null;

        if (StringUtils.isBlank(operator)) {
            return;
        }

        if ("and".equals(operator) || "or".equals(operator)) {
            final FilterParam childFilter = filterParam.addChild(operator, null, null);
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

    public static SummaryParam[] toSummaryParams(final ArrayNode summaryParamsNode) {
        final List<SummaryParam> params = new ArrayList<>();
        final int size = summaryParamsNode != null ? summaryParamsNode.size() : 0;

        for (int i = 0; i < size; i++) {
            params.add(toSummaryParam(summaryParamsNode.get(i)));
        }

        return params.toArray(new SummaryParam[params.size()]);
    }

    public static SummaryParam toSummaryParam(final Object summaryParamNode) {
        return JacksonUtils.getObjectMapper().convertValue(summaryParamNode,
                SummaryParam.class);
    }

}
