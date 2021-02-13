/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
var pieData1 = [{
        "pname": "Lithuania",
        "value": 501.9
    }, {
        "pname": "Czech Republic",
        "value": 301.9
    }, {
        "pname": "Ireland",
        "value": 201.1
    }, {
        "pname": "Germany",
        "value": 165.8
    }, {
        "pname": "Australia",
        "value": 139.9
    }, {
        "pname": "Austria",
        "value": 128.3
    }, {
        "pname": "UK",
        "value": 199
    }, {
        "pname": "Belgium",
        "value": 60
    }, {
        "pname": "The Netherlands",
        "value": 50
    }];
var charts = {};
var Chart1;
var pieChart1;
function createPieChart(divId) {
    var chart = AmCharts.makeChart(divId, {
        "type": "pie",
        "theme": "light",
        "dataProvider": pieData1,
        "valueField": "value",
        "titleField": "pname",
        "balloon": {
            "fixedPosition": true
        },
        "export": {
            "enabled": true
        }
    });
}
function createChart(divId) {
    Chart1 = AmCharts.makeChart(divId, {
        "type": "serial",
        "theme": "light",
        "legend": {
            align: "right",
            valueWidth: 0,
            spacing: 10,
            markerSize: 14,
            fontSize: 12,
            //            "useGraphSettings": true
        },
        "dataProvider": chartData1,
        "synchronizeGrid": true,
        "graphs": graphs1[0],
        //        "chartScrollbar": {},
        "chartCursor": {
            "cursorPosition": "mouse"
        },
        "valueScrollbar": {
            "oppositeAxis": false,
            "offset": 50,
            "scrollbarHeight": 35
        },

        "categoryField": "time",
        "categoryAxis": {
            gridCount: 6,
            labelFrequency: 1,
            equalSpacing: true,
            "axisColor": "#DADADA",
            "minorGridEnabled": false
        },
        "export": {
            "enabled": true,
            "position": "top-right"
        }
    });
}




