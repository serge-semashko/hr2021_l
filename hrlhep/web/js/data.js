/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

var graphs1 = [
    [
        {
            "lineColor": "#29A2CC",
            "bullet": "round", "lineThickness": 3,
            "bulletBorderThickness": 1,
            "hideBulletsCount": 1,
            "title": "T6",
            "valueField": "t6",
            "fillAlphas": 0
        },
        {
            "lineColor": "#250acb",
            "bullet": "round", "lineThickness": 3,
            "bulletBorderThickness": 1,
            "hideBulletsCount": 1,
            "title": "T11",
            "valueField": "t11",
            "fillAlphas": 0
        },
        {
            "lineColor": "#66CC00",
            "bullet": "round", "lineThickness": 3,
            "bulletBorderThickness": 1,
            "hideBulletsCount": 1,
            "title": "T12",
            "valueField": "t12",
            "fillAlphas": 0
        },
        {
            "lineColor": "#FF3300",
            "bullet": "round", "lineThickness": 3,
            "bulletBorderThickness": 1,
            "hideBulletsCount": 1,
            "title": "T13",
            "valueField": "k1t13",
            disableed: true,
            visible: false,
            "fillAlphas": 0
        }]
            ,
    [
        {
            "lineColor": "#29A2CC",
            "bullet": "round", "lineThickness": 3,
            "bulletBorderThickness": 1,
            "hideBulletsCount": 1,
            "title": "T6",
            "valueField": "k1t6",
            "fillAlphas": 0
        },
        {
            "lineColor": "#250acb",
            "bullet": "round", "lineThickness": 3,
            "bulletBorderThickness": 1,
            "hideBulletsCount": 1,
            "title": "T7",
            "valueField": "k1t7",
            "fillAlphas": 0
        },
        {
            "lineColor": "#66CC00",
            "bullet": "round", "lineThickness": 3,
            "bulletBorderThickness": 1,
            "hideBulletsCount": 1,
            "title": "dT5-6",
            "valueField": "k1dt5t6",
            "fillAlphas": 0
        },
        {
            "lineColor": "#FF3300",
            "bullet": "round", "lineThickness": 3,
            "bulletBorderThickness": 1,
            "hideBulletsCount": 1,
            "title": "dT3-4",
            "valueField": "k1dt3t4",
            disableed: true,
            visible: false,
            "fillAlphas": 0
        }]
];

var chartData1 = [
    {time: "04:21:16", date: "28:10:17", k1t6: 80.44, k1t7: 196.96, k1t11: 245.26, k1t12: 248.07, k1t13: 289.10, k1t5: 86.67, k1t3: 86.62, k1t4: 85.10, k1dt5t6: 6.23, k1dt3t4: 1.52, k1sbr: 0, k1vanna: 0, k1bo1: 78.82, k1bo2: 0, sbrkgu1: 0, k2t6: 92.57, k2t7: 177.48, k2t11: 223.92, k2t12: 224.79, k2t13: 287.98, k2t5: 91.26, k2t3: 91.76, k2t4: 97.48, k2dt5t6: -1.31, k2dt3t4: -5.72, k2sbr: 0, k2vanna: 0, k2bo1: 90.64, k2bo2: 0, sbrkgu2: 0},
    {time: "04:21:28", date: "28:10:17", k1t6: 80.47, k1t7: 196.96, k1t11: 245.26, k1t12: 248.30, k1t13: 289.06, k1t5: 86.67, k1t3: 86.62, k1t4: 85.24, k1dt5t6: 6.20, k1dt3t4: 1.38, k1sbr: 0, k1vanna: 0, k1bo1: 78.83, k1bo2: 0, sbrkgu1: 0, k2t6: 92.30, k2t7: 177.61, k2t11: 223.92, k2t12: 224.41, k2t13: 287.98, k2t5: 91.15, k2t3: 91.61, k2t4: 97.48, k2dt5t6: -1.15, k2dt3t4: -5.87, k2sbr: 0.00, k2vanna: 0, k2bo1: 87.07, k2bo2: 0, sbrkgu2: 0.00},
    {time: "04:21:40", date: "28:10:17", k1t6: 80.47, k1t7: 196.96, k1t11: 245.26, k1t12: 248.07, k1t13: 289.06, k1t5: 86.67, k1t3: 86.62, k1t4: 85.10, k1dt5t6: 6.20, k1dt3t4: 1.52, k1sbr: 0, k1vanna: 0, k1bo1: 78.82, k1bo2: 0, sbrkgu1: 0, k2t6: 92.57, k2t7: 177.61, k2t11: 223.92, k2t12: 224.79, k2t13: 287.98, k2t5: 91.10, k2t3: 91.61, k2t4: 97.48, k2dt5t6: -1.47, k2dt3t4: -5.87, k2sbr: 0, k2vanna: 0, k2bo1: 91.52, k2bo2: 0, sbrkgu2: 0},
    {time: "04:21:53", date: "28:10:17", k1t6: 80.44, k1t7: 196.96, k1t11: 245.26, k1t12: 248.07, k1t13: 289.10, k1t5: 86.67, k1t3: 86.62, k1t4: 85.12, k1dt5t6: 6.23, k1dt3t4: 1.50, k1sbr: 0, k1vanna: 0, k1bo1: 78.82, k1bo2: 0, sbrkgu1: 0, k2t6: 92.57, k2t7: 177.61, k2t11: 223.92, k2t12: 224.68, k2t13: 287.98, k2t5: 91.29, k2t3: 91.57, k2t4: 97.39, k2dt5t6: -1.28, k2dt3t4: -5.82, k2sbr: 0, k2vanna: 0, k2bo1: 90.92, k2bo2: 0, sbrkgu2: 0},
    {time: "04:22:05", date: "28:10:17", k1t6: 80.21, k1t7: 196.96, k1t11: 245.26, k1t12: 248.30, k1t13: 289.06, k1t5: 86.67, k1t3: 86.62, k1t4: 85.10, k1dt5t6: 6.46, k1dt3t4: 1.52, k1sbr: 0, k1vanna: 0, k1bo1: 78.83, k1bo2: 0, sbrkgu1: 0, k2t6: 92.57, k2t7: 177.48, k2t11: 223.92, k2t12: 224.79, k2t13: 287.98, k2t5: 91.35, k2t3: 91.57, k2t4: 97.43, k2dt5t6: -1.22, k2dt3t4: -5.86, k2sbr: 0, k2vanna: 0, k2bo1: 88.05, k2bo2: 0, sbrkgu2: 0},
    {time: "04:22:17", date: "28:10:17", k1t6: 80.47, k1t7: 196.96, k1t11: 245.26, k1t12: 248.07, k1t13: 289.10, k1t5: 86.67, k1t3: 86.62, k1t4: 85.10, k1dt5t6: 6.20, k1dt3t4: 1.52, k1sbr: 0, k1vanna: 0, k1bo1: 78.83, k1bo2: 0, sbrkgu1: 0, k2t6: 92.53, k2t7: 177.61, k2t11: 223.92, k2t12: 224.41, k2t13: 287.98, k2t5: 91.24, k2t3: 91.57, k2t4: 97.48, k2dt5t6: -1.29, k2dt3t4: -5.91, k2sbr: 0, k2vanna: 0, k2bo1: 87.30, k2bo2: 0, sbrkgu2: 0},
    {time: "04:22:28", date: "28:10:17", k1t6: 80.25, k1t7: 196.96, k1t11: 245.26, k1t12: 248.07, k1t13: 289.10, k1t5: 86.67, k1t3: 86.62, k1t4: 85.10, k1dt5t6: 6.42, k1dt3t4: 1.52, k1sbr: 0, k1vanna: 0, k1bo1: 78.83, k1bo2: 0, sbrkgu1: 0, k2t6: 92.57, k2t7: 177.61, k2t11: 223.92, k2t12: 224.68, k2t13: 287.98, k2t5: 91.32, k2t3: 91.79, k2t4: 97.48, k2dt5t6: -1.25, k2dt3t4: -5.69, k2sbr: 0.00, k2vanna: 0, k2bo1: 85.58, k2bo2: 0, sbrkgu2: 0.00},
    {time: "04:22:40", date: "28:10:17", k1t6: 80.47, k1t7: 196.96, k1t11: 245.26, k1t12: 248.07, k1t13: 289.04, k1t5: 86.67, k1t3: 86.62, k1t4: 85.10, k1dt5t6: 6.20, k1dt3t4: 1.52, k1sbr: 0, k1vanna: 0, k1bo1: 78.83, k1bo2: 0, sbrkgu1: 0, k2t6: 92.57, k2t7: 177.61, k2t11: 223.92, k2t12: 224.15, k2t13: 287.98, k2t5: 91.12, k2t3: 91.64, k2t4: 97.39, k2dt5t6: -1.45, k2dt3t4: -5.75, k2sbr: 0, k2vanna: 0, k2bo1: 92.36, k2bo2: 0, sbrkgu2: 0},
    {time: "04:22:52", date: "28:10:17", k1t6: 80.73, k1t7: 196.96, k1t11: 245.26, k1t12: 248.07, k1t13: 289.10, k1t5: 86.67, k1t3: 86.62, k1t4: 85.10, k1dt5t6: 5.94, k1dt3t4: 1.52, k1sbr: 0, k1vanna: 0, k1bo1: 78.83, k1bo2: 0, sbrkgu1: 0, k2t6: 92.57, k2t7: 177.61, k2t11: 223.92, k2t12: 224.04, k2t13: 287.98, k2t5: 91.29, k2t3: 91.57, k2t4: 97.48, k2dt5t6: -1.28, k2dt3t4: -5.91, k2sbr: 0, k2vanna: 0, k2bo1: 90.19, k2bo2: 0, sbrkgu2: 0},
    {time: "04:23:04", date: "28:10:17", k1t6: 80.25, k1t7: 196.96, k1t11: 245.26, k1t12: 248.07, k1t13: 289.10, k1t5: 86.67, k1t3: 86.62, k1t4: 85.22, k1dt5t6: 6.42, k1dt3t4: 1.40, k1sbr: 0, k1vanna: 0, k1bo1: 78.82, k1bo2: 0, sbrkgu1: 0, k2t6: 92.30, k2t7: 177.48, k2t11: 223.92, k2t12: 224.04, k2t13: 287.98, k2t5: 91.12, k2t3: 91.67, k2t4: 97.48, k2dt5t6: -1.18, k2dt3t4: -5.81, k2sbr: 0.01, k2vanna: 0, k2bo1: 87.23, k2bo2: 0, sbrkgu2: 0.01},
    {time: "04:23:16", date: "28:10:17", k1t6: 80.47, k1t7: 196.96, k1t11: 245.26, k1t12: 248.07, k1t13: 289.10, k1t5: 86.67, k1t3: 86.62, k1t4: 85.10, k1dt5t6: 6.20, k1dt3t4: 1.52, k1sbr: 0, k1vanna: 0, k1bo1: 78.81, k1bo2: 0, sbrkgu1: 0, k2t6: 92.57, k2t7: 177.61, k2t11: 223.92, k2t12: 224.79, k2t13: 288.02, k2t5: 91.29, k2t3: 91.57, k2t4: 97.48, k2dt5t6: -1.28, k2dt3t4: -5.91, k2sbr: 0, k2vanna: 0, k2bo1: 90.85, k2bo2: 0, sbrkgu2: 0},
    {time: "04:23:28", date: "28:10:17", k1t6: 80.44, k1t7: 196.96, k1t11: 245.26, k1t12: 248.07, k1t13: 289.10, k1t5: 86.67, k1t3: 86.62, k1t4: 85.22, k1dt5t6: 6.23, k1dt3t4: 1.40, k1sbr: 0, k1vanna: 0, k1bo1: 78.82, k1bo2: 0, sbrkgu1: 0, k2t6: 92.22, k2t7: 177.61, k2t11: 223.92, k2t12: 224.04, k2t13: 287.98, k2t5: 91.24, k2t3: 91.61, k2t4: 97.48, k2dt5t6: -0.98, k2dt3t4: -5.87, k2sbr: 0, k2vanna: 0, k2bo1: 86.98, k2bo2: 0, sbrkgu2: 0},
    {time: "04:23:40", date: "28:10:17", k1t6: 80.47, k1t7: 196.96, k1t11: 245.26, k1t12: 248.30, k1t13: 289.10, k1t5: 86.76, k1t3: 86.62, k1t4: 85.10, k1dt5t6: 6.29, k1dt3t4: 1.52, k1sbr: 0, k1vanna: 0, k1bo1: 78.83, k1bo2: 0, sbrkgu1: 0, k2t6: 92.57, k2t7: 177.61, k2t11: 223.92, k2t12: 224.41, k2t13: 288.02, k2t5: 91.15, k2t3: 91.57, k2t4: 97.48, k2dt5t6: -1.42, k2dt3t4: -5.91, k2sbr: 0, k2vanna: 0, k2bo1: 92.09, k2bo2: 0, sbrkgu2: 0},
    {time: "04:23:52", date: "28:10:17", k1t6: 80.47, k1t7: 196.96, k1t11: 245.26, k1t12: 248.07, k1t13: 289.10, k1t5: 86.67, k1t3: 86.62, k1t4: 85.10, k1dt5t6: 6.20, k1dt3t4: 1.52, k1sbr: 0, k1vanna: 0, k1bo1: 78.83, k1bo2: 0, sbrkgu1: 0, k2t6: 92.57, k2t7: 177.61, k2t11: 223.92, k2t12: 224.04, k2t13: 287.98, k2t5: 91.26, k2t3: 91.63, k2t4: 97.48, k2dt5t6: -1.31, k2dt3t4: -5.85, k2sbr: 0.00, k2vanna: 0, k2bo1: 90.53, k2bo2: 0, sbrkgu2: 0.00},
    {time: "04:24:04", date: "28:10:17", k1t6: 80.30, k1t7: 196.96, k1t11: 245.26, k1t12: 248.07, k1t13: 289.10, k1t5: 86.98, k1t3: 86.62, k1t4: 85.10, k1dt5t6: 6.68, k1dt3t4: 1.52, k1sbr: 0, k1vanna: 0, k1bo1: 78.77, k1bo2: 0, sbrkgu1: 0, k2t6: 92.53, k2t7: 177.61, k2t11: 223.92, k2t12: 224.79, k2t13: 287.98, k2t5: 91.15, k2t3: 91.57, k2t4: 97.48, k2dt5t6: -1.38, k2dt3t4: -5.91, k2sbr: 0, k2vanna: 0, k2bo1: 88.10, k2bo2: 0, sbrkgu2: 0},
    {time: "04:24:16", date: "28:10:17", k1t6: 80.47, k1t7: 196.96, k1t11: 245.26, k1t12: 248.19, k1t13: 289.10, k1t5: 86.67, k1t3: 86.62, k1t4: 85.17, k1dt5t6: 6.20, k1dt3t4: 1.45, k1sbr: 0, k1vanna: 0, k1bo1: 78.78, k1bo2: 0, sbrkgu1: 0, k2t6: 92.57, k2t7: 177.61, k2t11: 223.92, k2t12: 224.41, k2t13: 287.98, k2t5: 91.29, k2t3: 91.57, k2t4: 97.48, k2dt5t6: -1.28, k2dt3t4: -5.91, k2sbr: 0, k2vanna: 0, k2bo1: 87.50, k2bo2: 0, sbrkgu2: 0},
    {time: "04:24:51", date: "28:10:17", k1t6: 80.25, k1t7: 196.96, k1t11: 245.26, k1t12: 248.07, k1t13: 289.10, k1t5: 86.45, k1t3: 86.62, k1t4: 85.10, k1dt5t6: 6.20, k1dt3t4: 1.52, k1sbr: 0, k1vanna: 0, k1bo1: 78.77, k1bo2: 0, sbrkgu1: 0, k2t6: 92.57, k2t7: 177.61, k2t11: 223.92, k2t12: 224.79, k2t13: 287.98, k2t5: 91.29, k2t3: 91.57, k2t4: 97.48, k2dt5t6: -1.28, k2dt3t4: -5.91, k2sbr: 0, k2vanna: 0, k2bo1: 86.24, k2bo2: 0, sbrkgu2: 0},
    {time: "04:24:28", date: "28:10:17", k1t6: 80.47, k1t7: 196.96, k1t11: 245.26, k1t12: 248.87, k1t13: 289.04, k1t5: 86.67, k1t3: 86.62, k1t4: 85.10, k1dt5t6: 6.20, k1dt3t4: 1.52, k1sbr: 0, k1vanna: 0, k1bo1: 78.77, k1bo2: 0, sbrkgu1: 0, k2t6: 92.30, k2t7: 177.61, k2t11: 223.92, k2t12: 224.41, k2t13: 287.98, k2t5: 91.29, k2t3: 91.76, k2t4: 97.48, k2dt5t6: -1.01, k2dt3t4: -5.72, k2sbr: 0, k2vanna: 0, k2bo1: 85.51, k2bo2: 0, sbrkgu2: 0},
    {time: "04:24:40", date: "28:10:17", k1t6: 80.47, k1t7: 196.96, k1t11: 245.26, k1t12: 248.87, k1t13: 289.04, k1t5: 86.67, k1t3: 86.62, k1t4: 85.10, k1dt5t6: 6.20, k1dt3t4: 1.52, k1sbr: 0, k1vanna: 0, k1bo1: 78.78, k1bo2: 0, sbrkgu1: 0, k2t6: 92.30, k2t7: 177.61, k2t11: 223.92, k2t12: 224.79, k2t13: 287.98, k2t5: 91.29, k2t3: 91.57, k2t4: 97.56, k2dt5t6: -1.01, k2dt3t4: -5.99, k2sbr: 0, k2vanna: 0, k2bo1: 94.47, k2bo2: 0, sbrkgu2: 0},
    {time: "04:25:03", date: "28:10:17", k1t6: 80.34, k1t7: 196.96, k1t11: 245.26, k1t12: 248.07, k1t13: 289.10, k1t5: 86.67, k1t3: 86.62, k1t4: 85.10, k1dt5t6: 6.33, k1dt3t4: 1.52, k1sbr: 0, k1vanna: 0, k1bo1: 78.77, k1bo2: 0, sbrkgu1: 0, k2t6: 92.57, k2t7: 177.61, k2t11: 223.92, k2t12: 224.41, k2t13: 287.98, k2t5: 91.15, k2t3: 91.76, k2t4: 97.48, k2dt5t6: -1.42, k2dt3t4: -5.72, k2sbr: 0, k2vanna: 0, k2bo1: 87.51, k2bo2: 0, sbrkgu2: 0},
    {time: "04:25:14", date: "28:10:17", k1t6: 80.44, k1t7: 196.96, k1t11: 245.26, k1t12: 248.07, k1t13: 289.10, k1t5: 86.67, k1t3: 86.62, k1t4: 85.17, k1dt5t6: 6.23, k1dt3t4: 1.45, k1sbr: 0, k1vanna: 0, k1bo1: 78.78, k1bo2: 0, sbrkgu1: 0, k2t6: 92.57, k2t7: 177.61, k2t11: 223.92, k2t12: 224.41, k2t13: 287.98, k2t5: 91.15, k2t3: 91.57, k2t4: 97.48, k2dt5t6: -1.42, k2dt3t4: -5.91, k2sbr: 0, k2vanna: 0, k2bo1: 94.06, k2bo2: 0, sbrkgu2: 0},
    {time: "04:25:26", date: "28:10:17", k1t6: 80.21, k1t7: 196.96, k1t11: 245.26, k1t12: 248.07, k1t13: 289.04, k1t5: 86.67, k1t3: 86.62, k1t4: 85.10, k1dt5t6: 6.46, k1dt3t4: 1.52, k1sbr: 0, k1vanna: 0, k1bo1: 78.79, k1bo2: 0, sbrkgu1: 0, k2t6: 92.57, k2t7: 177.61, k2t11: 223.92, k2t12: 224.04, k2t13: 287.98, k2t5: 91.32, k2t3: 91.61, k2t4: 97.48, k2dt5t6: -1.25, k2dt3t4: -5.87, k2sbr: 0, k2vanna: 0, k2bo1: 89.38, k2bo2: 0, sbrkgu2: 0},
    {time: "04:25:38", date: "28:10:17", k1t6: 80.25, k1t7: 196.96, k1t11: 245.26, k1t12: 248.07, k1t13: 289.10, k1t5: 86.67, k1t3: 86.62, k1t4: 85.22, k1dt5t6: 6.42, k1dt3t4: 1.40, k1sbr: 0, k1vanna: 0, k1bo1: 78.78, k1bo2: 0, sbrkgu1: 0, k2t6: 92.33, k2t7: 177.61, k2t11: 223.92, k2t12: 224.04, k2t13: 287.98, k2t5: 91.10, k2t3: 91.57, k2t4: 97.48, k2dt5t6: -1.23, k2dt3t4: -5.91, k2sbr: 0, k2vanna: 0, k2bo1: 87.27, k2bo2: 0, sbrkgu2: 0},
    {time: "04:25:50", date: "28:10:17", k1t6: 80.34, k1t7: 196.96, k1t11: 245.26, k1t12: 248.07, k1t13: 289.06, k1t5: 86.67, k1t3: 86.62, k1t4: 85.10, k1dt5t6: 6.33, k1dt3t4: 1.52, k1sbr: 0, k1vanna: 0, k1bo1: 78.78, k1bo2: 0, sbrkgu1: 0, k2t6: 92.57, k2t7: 177.61, k2t11: 223.92, k2t12: 224.68, k2t13: 287.98, k2t5: 91.29, k2t3: 91.57, k2t4: 97.48, k2dt5t6: -1.28, k2dt3t4: -5.91, k2sbr: 0, k2vanna: 0, k2bo1: 87.42, k2bo2: 0, sbrkgu2: 0},
    {time: "04:26:02", date: "28:10:17", k1t6: 80.47, k1t7: 197.14, k1t11: 245.26, k1t12: 248.76, k1t13: 289.05, k1t5: 86.67, k1t3: 86.62, k1t4: 85.10, k1dt5t6: 6.20, k1dt3t4: 1.52, k1sbr: 0, k1vanna: 0, k1bo1: 78.78, k1bo2: 0, sbrkgu1: 0, k2t6: 92.43, k2t7: 177.61, k2t11: 223.92, k2t12: 224.79, k2t13: 287.98, k2t5: 91.15, k2t3: 91.57, k2t4: 97.48, k2dt5t6: -1.28, k2dt3t4: -5.91, k2sbr: 0, k2vanna: 0, k2bo1: 92.92, k2bo2: 0, sbrkgu2: 0},
    {time: "04:26:14", date: "28:10:17", k1t6: 80.47, k1t7: 196.96, k1t11: 245.26, k1t12: 248.07, k1t13: 289.10, k1t5: 86.76, k1t3: 86.62, k1t4: 85.10, k1dt5t6: 6.29, k1dt3t4: 1.52, k1sbr: 0, k1vanna: 0, k1bo1: 78.79, k1bo2: 0, sbrkgu1: 0, k2t6: 92.57, k2t7: 177.61, k2t11: 223.92, k2t12: 224.79, k2t13: 287.99, k2t5: 91.15, k2t3: 91.57, k2t4: 97.48, k2dt5t6: -1.42, k2dt3t4: -5.91, k2sbr: 0, k2vanna: 0, k2bo1: 87.64, k2bo2: 0, sbrkgu2: 0},
    {time: "04:26:26", date: "28:10:17", k1t6: 80.47, k1t7: 196.96, k1t11: 245.26, k1t12: 248.07, k1t13: 289.10, k1t5: 86.67, k1t3: 86.62, k1t4: 85.10, k1dt5t6: 6.20, k1dt3t4: 1.52, k1sbr: 0, k1vanna: 0, k1bo1: 78.78, k1bo2: 0, sbrkgu1: 0, k2t6: 92.30, k2t7: 177.61, k2t11: 223.92, k2t12: 224.79, k2t13: 287.99, k2t5: 91.41, k2t3: 91.57, k2t4: 97.48, k2dt5t6: -0.89, k2dt3t4: -5.91, k2sbr: 0, k2vanna: 0, k2bo1: 90.44, k2bo2: 0, sbrkgu2: 0},
    {time: "04:26:37", date: "28:10:17", k1t6: 80.47, k1t7: 197.50, k1t11: 245.26, k1t12: 248.07, k1t13: 289.10, k1t5: 86.67, k1t3: 86.62, k1t4: 85.17, k1dt5t6: 6.20, k1dt3t4: 1.45, k1sbr: 0, k1vanna: 0, k1bo1: 78.78, k1bo2: 0, sbrkgu1: 0, k2t6: 92.33, k2t7: 177.61, k2t11: 223.92, k2t12: 224.79, k2t13: 287.98, k2t5: 91.29, k2t3: 91.57, k2t4: 97.48, k2dt5t6: -1.04, k2dt3t4: -5.91, k2sbr: 0, k2vanna: 0, k2bo1: 87.93, k2bo2: 0, sbrkgu2: 0},
    {time: "04:26:49", date: "28:10:17", k1t6: 80.47, k1t7: 196.96, k1t11: 245.26, k1t12: 248.07, k1t13: 289.10, k1t5: 86.67, k1t3: 86.62, k1t4: 85.10, k1dt5t6: 6.20, k1dt3t4: 1.52, k1sbr: 0, k1vanna: 0, k1bo1: 78.79, k1bo2: 0, sbrkgu1: 0, k2t6: 92.53, k2t7: 177.61, k2t11: 223.92, k2t12: 225.07, k2t13: 287.98, k2t5: 91.35, k2t3: 91.57, k2t4: 97.48, k2dt5t6: -1.18, k2dt3t4: -5.91, k2sbr: 0, k2vanna: 0, k2bo1: 87.07, k2bo2: 0, sbrkgu2: 0}
];
