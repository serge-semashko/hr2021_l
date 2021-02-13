sys/stresstest.mod

[parameters]
title=***Запрос
debug=off
[end]


[report]
$INCLUDE [report_] ??AR_SYS_ADMIN=1|k=doit
[end]

[URLs]
    #ServletPath#?c=docs/view_doc&doc_id=20828&mode=ext
    #ServletPath#?c=wf/print_wf_old&doc_id=20449&mode=ext
    #ServletPath# ??
[end]

[URLs-ADB]
    #ServletPath#
    #ServletPath#?c=doc/list 
    #ServletPath#?c=doc/docEdit&type=ext&stype=&DOC_ID=373162
[end]

[report_]
<html>
    <head>
        <TITLE>SED-stress</TITLE>
        <script type="text/javascript" src="#jsPath#jquery-1.9.1.min.js"></script>
        <script type="text/javascript" src="#jsPath#canvasjs.min.js"></script>
        <script src="https://canvasjs.com/assets/script/canvasjs.min.js"></script> ??
            https://canvasjs.com/javascript-charts/ ??
            https://canvasjs.com/docs/charts/chart-options/colorset/ ??
        <style>
            .out_div {height:100px; width:100%; border:solid 1px green; overflow:auto;}
            .url {color:white;}
        </style>

$SET_PARAMETERS nt=9; ??!nt
$SET_PARAMETERS numThreads=#nt#; 

        <script>
            var numRequests = 10;
            var currRequest = 0;
            var numThreads=#numThreads#; 
            var URLs=[];
            var totalTime = 0;
            var avgTime = 0;
            var minTime = 1000000;
            var maxTime = 0;
            var startTm = 0;
            var nextUrlId=-1;
            var paused = false;
            var chartRenderN = 10;

            var start=function(){
                var s = document.theForm.URLs.value.split("\n"); 
                URLs=[];
                for(i=0; i<s.length; i++) {
                    if(s[i].trim().length > 1)
                        URLs.push(s[i]);
                }

                numRequests = parseInt(document.theForm.numRequests.value);
                currRequest=0;
                totalTime = 0;
                startTm=(new Date()).getTime();

                prepareChart();
                chartRenderN = 10;

                for(i=1; i<=numThreads; i++) {  // цикл по iframe
                    loadNext(i);
                }
                document.theForm.Go.disabled=true;   
                document.theForm.Stop.disabled=false;
            }

            var pause = function(){
                paused = !paused;
                if(!paused)
                    for(i=1; i<=numThreads; i++) {  // цикл по iframe
                        scheduleNext(i);
                    }
            }

            var scheduleNext=function(threadNum) {
                if(paused) 
                    renderChart(); 
                else {
                    if(numRequests % 100 < 2*#nt#) {  
                        renderChart(); ??
                        window.setTimeout(loadNext, 3000, threadNum);  //раз в 100 запросов даем томкату и браузеру "отдохнуть" 3 секунды 
                        window.setTimeout(loadNext, 60000, threadNum);  //раз в 100 запросов даем томкату "отдохнуть" минутку ??
                    }
                    else {
                        window.setTimeout(loadNext, 100, threadNum);
                    }
                }
            }

            var loadNext=function(threadNum) {
                if(numRequests-- > 0) {
                    if(++nextUrlId >= URLs.length) nextUrlId = 0;
                    eval("url_id" + threadNum + "=" + nextUrlId);
                    console.log("loadNext threadNum=" + threadNum + "; url_id=" + nextUrlId + ";");  ??
                    eval("t0" + threadNum + "=(new Date()).getTime();"); // засекаем время для iframe threadNum
                    $("##tm" + threadNum).css( "background-color", colors[nextUrlId]); // раскраска span таймера iframe
                    $("##url" + threadNum).html(URLs[nextUrlId]);
                    var a = "?";
                    if(URLs[nextUrlId].includes("?")) a = "&";
                    frames[threadNum-1].window.location.replace(URLs[nextUrlId] +  a + "anti-cache=" + (new Date()).getTime()); 
                }
                else {
                    stop();
                }
            }

            var stop=function(){
                numRequests=0; 
                document.theForm.Go.disabled=false; 
                document.theForm.Stop.disabled=true;
                renderChart();
            }
        </script>
    </head>

    <body style="background-color:##e0e0e0;">
        <b><br><center>STRESS TESTER</center></b>

        <form name="theForm" method="POST" enctype="multipart/form-data">
            <input type=hidden name="c" value="#c#">
            <input type=hidden name="k" value="#k#">
            <textarea id="URLs" name="URLs" style="width:800px; height:80px;">
                $INCLUDE [URLs]
            </textarea>

            nt=#numThreads#; 
            numRequests: <input size=4 name="numRequests" value="10">
            <input type=button name="Go" value=" Go! " onClick="start(); ">
            <input type=button name="Pause" value=" Pause " onClick="pause(); ">
            <input type=button name="Stop" value=" Stop! " disabled onClick="stop();">
        </form>
        
        <div id="chartContainer" style="height: 250px; border:solid 1px grey; width: 90%;"></div>
        <div id="timers" style="border:solid 1px black; background-color:white; font-size:15px; font-weight:bold; padding:5px; margin:5px; display:inline-block;"></div>
<br>
        $JS var numThreads=#numThreads#;
        $JS_BEGIN       
            for (i=1; i<= numThreads; i++) {
                BT.addParameter("thread", i);
                BT.getCustomSection("", "block", out);
            }
        $JS_END
        <hr>

        $INCLUDE [chart script]

    </body>
</html>
[end]


[block]
    <div style="border:solid 1px grey; background-color:white; margin:2px; font-size:13px; float:left; width:500px; height:140px;">
        <b>#thread#:</b>
        <span id="tm#thread#" class="url">tm#thread#</span> 
        <span id="url#thread#" class="" style="font-size:11px;">url#thread#</span>
        <br>
        <iframe id="out#thread#" name="out#thread#" class="out_div" ></iframe>
    </div>

    <script>
        var t0#thread# = 0;
        var t#thread# = 0;
        var url_id#thread# = 1;
/**
 * Обработчик onLoad для frame
 */
        document.getElementById('out#thread#').onload = function() { 
            var tm = (new Date()).getTime();
            t#thread# = tm - t0#thread#;   
            $("##tm#thread#").html( t#thread# + "ms");
            totalTime += t#thread#;
            if(t#thread# > 29900) t#thread# = 29900;
            avgTime =  Math.floor(totalTime / ++currRequest);
            if(minTime > t#thread#) minTime = t#thread#;
            if(maxTime < t#thread#) maxTime = t#thread#;

            $("##timers").html( currRequest + ": AVG.=" + avgTime + "ms; Min=" + minTime + "; max=" + maxTime 
                + "; (total time: " + Math.floor(totalTime/1000) 
                + " / " + Math.floor((tm - startTm) / 1000) + "s.)");
    console.log("********** #thread# url_id#thread#=" + url_id#thread# + "; color=" + colors[url_id#thread#] + ";"); ??

            if(t#thread# > 29900) t#thread# = 29900; ??
            addChartPoint(currRequest, t#thread# / 1000, colors[url_id#thread#], #thread#);
            scheduleNext(#thread#);
        } 
    </script>
[end]


[chart script]
<script>
    var data = [];
    var dataSeries = { type: "line" }; ??
    var dataSeries = { type: "scatter" };
    var dataPoints = [];
    var chart;
    var colors = [
             "##A00000",
             "##008000",
             "##0000F0",
             "##000000",
             "##000000",
             "##000000"
            ]; 

    var prepareChart=function(){
        data = [];
        dataPoints = [];
        var xmax = numRequests + 1;    
        var options = {
                zoomEnabled: true,
                animationEnabled: false,
                title: {
                        text: "Responce time ms."
                },
                axisX:{
                        minimum: 0, 
                        maximum: xmax
                },

                axisY: {
                        includeZero: true,
                        lineThickness: 0
                },
                toolTipContent: {name}: {y} , ??
                data: data 
        };
        chart = new CanvasJS.Chart("chartContainer", options);
    }

    var addChartPoint=function(x,y,color, threadNum){
            dataPoints.push({
                    x: x,
                    y: y,
                    color: color, 
                    markerSize: 3,
                    name: threadNum,
                    label: threadNum + ": " + x ??
                    toolTipContent: "{name} / {x}: {y}"
            });
            if(dataPoints.length % chartRenderN == 1) 
            {
                renderChart(); 
                if(chartRenderN < 100)
                    chartRenderN +=10;
            }

    }

    var renderChart=function(){
        console.log(" renderChart: dataPoints.size=" + dataPoints.length + ";");
        dataSeries.dataPoints = dataPoints;
        data.push(dataSeries);
        chart.render();
    }
</script>
[end]

