samples/include.cfg

[comments]
descr=Описание директивы $INCLUDE
input=
output=
parents=tab_samples.cfg
childs=
testURL=?c=samples/include&ajax=Y
author=Куняев
[end]

[description]
Описание директивы $INCLUDE
[end]


[parameters]
title=$INCLUDE
[end]

[report]
$INCLUDE samples/common.dat[start]

<h3>Директива $INCLUDE</h3>

<fieldset class="code bg_white"><legend>Описание:</legend>

Директива $INCLUDE вставляет в точку, где она встретилась в процессе интерпретации кода модуля, содержимое заданной секции из текущего модуля либо из другого файла.<br><br>
В процессе вставки производится построчная интерпретация содержимого вставляемой секции по общим правилам:
<ul>
<li>подстановка значений параметров</li>
<li>условное включение строк</li>
<li>обработка всех директив, включая $INCLUDE</li>
</ul>
Таким образом, возможно использовать директиву $INCLUDE не только для вставки строк в выходной поток, 
но и для выполнения определенных действий с помощью директив &##36;GET_DATA, &##36;SET_PARAMETERS и других.
</p>

Непосредственно в директиве можно присвоить значения временным параметрам для их использования в теле секции. 
Эти параметры очищаются при выходе из секции, а также при выполнении вложенной директивы $INCLUDE<br>
</fieldset>

<fieldset class="code bg_white"><legend>Синтаксис:</legend>
&##36;INCLUDE [filepath][sectionName] [param: name1=value1; name2=value2; ...]<br><br>
<b>filepath</b> - путь к файлу, содержащему секцию, относительно корня пути к модулям. <b>Не обязателен.</b> 
По умолчанию секция ищется в текущем модуле, заданном в параметре "с" http-запроса.
<br><br>
<b>sectionName</b> - имя секции
<br><br>
<b>param: name1=value1; name2=value2; ...</b> - Список параметров, значения которых необходимо установить для использования в теле секции. <b>Не обязателен.</b>
</fieldset>


<fieldset class="code"><legend>Примеры:</legend>

1. Вставить секцию [head] из файла ##CfgRootPath##/samples/common.dat: 
<div class="m20 bg_white">&##36;INCLUDE samples/common.dat[head]</div>
<hr>

2. Вставить секцию [set parameters] из текущего модуля (фактически - присвоить значения трем параметрам и выполнить запрос в БД): 
<div class="m20 bg_white">&##36;INCLUDE [set parameters]<br>
...<br>...<br>
&##91;end]
<br><br>

&##91;set parameters]<br>
&##36;SET_PARAMETERS p1=Значение 1; p2=Значение 2; <br>
&##36;SET_PARAMETERS_SESSION name=Значение сессионного параметра; <br>
&##36;GET_DATA [get some value SQL]; <br>

&##91;end]
</div>
<hr>

3. Установить временные значения параметров p1 и p2 и вставить секцию [check parameters] из текущего модуля: 
<div class="m20 bg_white">&##36;INCLUDE [check parameters] param: p1=Значение 1; p2=Значение 2;</div>

В этом случае параметры p1 и p2 должны быть объявлены в коде секции:
<div class="m20 bg_white">
&##91;check parameters] &nbsp; param: p1; p2; p3=[знач. по умолчанию];<br>
..... тело секции .....<br>.....<br>
&##36;LOG3 Проверяем значение параметров: p1=##p1##, p2=##p2##, p3=##p3##<br>

.....<br>
&##91;end]

</fieldset>

$INCLUDE samples/common.dat[bottom]

[end]

