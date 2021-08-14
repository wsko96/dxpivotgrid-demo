$(function() {
    var pivotGridChart = $("#pivotgrid-chart").dxChart({
        commonSeriesSettings: {
            type: "bar"
        },
        tooltip: {
            enabled: true,
            format: "currency",
            customizeTooltip: function(args) {
                return {
                    html: args.seriesName + " | Total<div class='currency'>" + args.valueText + "</div>"
                };
            }
        },
        size: {
            height: 200
        },
        adaptiveLayout: {
            width: 450
        }
    }).dxChart("instance");

    var pivotGridPaging = { index: 1, size: 5, rowGroups: [ { selector: 'region' }, { selector: 'city' } ] };

    var pivotGrid = $("#pivotgrid").dxPivotGrid({
        allowSortingBySummary: true,
        allowFiltering: true,
        showBorders: true,
        showColumnGrandTotals: true,
        showRowGrandTotals: true,
        showRowTotals: true,
        showColumnTotals: true,
        fieldChooser: {
            enabled: true,
            height: 400
        },
        dataSource: {
            fields: [{
                caption: "Region",
                width: 120,
                dataField: "region",
                area: "row",
                sortBySummaryField: "Total",
                expanded: true
            }, {
                caption: "City",
                dataField: "city",
                width: 150,
                area: "row"
            }, {
                dataField: "date",
                dataType: "date",
                area: "column",
                expanded: true
            }, {
                groupName: "date",
                groupInterval: "month",
                visible: false
            }, {
                caption: "Total",
                dataField: "amount",
                dataType: "number",
                summaryType: "sum",
                format: "currency",
                area: "data"
            }],
            // store: sales
            remoteOperations: true,
            load: function (loadOptions) {
                var d = $.Deferred();
                $.getJSON('/api/v1/sales', {
                    // Passing settings to the server
                    paging: JSON.stringify(pivotGridPaging),
                    // Pass if the remoteOperations option is set to true
                    take: loadOptions.take,
                    skip: loadOptions.skip,
                    group: loadOptions.group ? JSON.stringify(loadOptions.group) : "",
                    filter: loadOptions.filter ? JSON.stringify(loadOptions.filter) : "",
                    totalSummary: loadOptions.totalSummary ? JSON.stringify(loadOptions.totalSummary) : "",
                    groupSummary: loadOptions.groupSummary ? JSON.stringify(loadOptions.groupSummary) : ""
                }).done(function (result) {
                    // You can process the received data here
 
                    if("data" in result)
                        d.resolve(result.data, { summary: result.summary });
                    else
                        d.resolve(result);
                });
                return d.promise();
            }
        }
    }).dxPivotGrid("instance");

    pivotGrid.bindChart(pivotGridChart, {
        dataFieldsDisplayMode: "splitPanes",
        alternateDataFields: false
    });
});
