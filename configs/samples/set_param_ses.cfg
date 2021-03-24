samples/set_param.cfg

[comments]
descr=Описание директивы $SET_PARAMETERS_SESSION
input=
output=
parents=tab_samples.cfg
childs=
testURL=?c=samples/set_param_ses
author=Фуряева
[end]

[description]
Описание директивы $SET_PARAMETERS_SESSION
[end]


[parameters]
title=$SET_PARAMETERS_SESSION
[end]

[report]
$INCLUDE samples/common.dat[start] 

<h3>Директива $SET_PARAMETERS_SESSION</h3>
<fieldset class="code bg_white"><legend>Описание:</legend>

Директива $SET_PARAMETERS_SESSION задает значение параметра на время http-сессии клиента. 
<ul>
<li>подстановка</li>
<li>условное</li>
<li>обработка</li>
</ul>
Таким образом, возможно использовать директиву $SET_PARAMETERS_SESSION не только 
</p>

Непосредственно в директиве можно <br>
</fieldset>

<fieldset class="code bg_white"><legend>Синтаксис:</legend>
&##36;SET_PARAMETERS_SESSION {name}={value}<br><br>
<b>{name}</b> - имя параметра<br>
<b>{value}</b> - значение сессионного параметра
<br><br>
</fieldset>

<fieldset class="code"><legend>Примеры:</legend>

1. Задать значение 
<div class="m20 bg_white">&##36;SET_PARAMETERS_SESSION srn=1;</div>
2. Установить несколько значений по умолчанию на странице:
<div class="m20 bg_white">&##36;SET_PARAMETERS_SESSION srn=1; rpp=20;</div>
3. Установить значение переменной с условием:
<div class="m20 bg_white">&##36;SET_PARAMETERS_SESSION srn=1; ??!srn</div>
<br>
</fieldset>
$INCLUDE samples/common.dat[bottom]
[end]


