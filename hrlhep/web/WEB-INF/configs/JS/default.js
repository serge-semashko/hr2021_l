// Заполняет массив sqlColNames названиями столбцов в выборке
function getMonthName(month) { 
    return ["Январь","Февраль","Март","Апрель","Май","Июнь","Июль","Август","Сентябрь", "Октябрь","Ноябрь","Декабрь"][parseInt(month)-1]
}
//    return ["January","February","March","April","May","June","July","August","September", "October","November","December"][parseInt(month)-1]
function replaceAll(str, find, replace) {
    return str.replace(new RegExp(find, 'g'), replace);
}
function monthKvartal(month){
    month =+ month;
    switch (month) {
        case 101:return 'I Квартал';
        case 102:return 'II Квартал';
        case 103:return 'III Квартал';
        case 104:return 'IV Квартал';
    }
    if ((month>0)&(month<13) ) {
       return  getMonthName(month);
   }
   return 'Ошибка даты';
}
function getColNames(resultSet) {
    var metaData = resultSet.getMetaData();
    var numCols = metaData.getColumnCount();
    var sqlColNames = [];
    for (var i = 0; i < numCols; i++) {
        sqlColNames.push(metaData.getColumnLabel(i + 1));
    }
    return sqlColNames;
}
;
// Делает выборку из данных. SQl берется из sqlSection 
// sqlSection будет парсится как в $GETDATA
// Для каждой строки быборки заполняет строку параметров значениями и вызывает секцию execSection
// Полный аналог обработки секции [SQL] секцией [ITEM] в TableServiceSpecial
function getFio(fullFIO)
{
    var t = fullFIO.split(' ');
    var o = "";
    var shortFIO = t[0];
    if (typeof t[1] != "string") {
    } else {
        shortFIO += " " + t[1].charAt(0) + ".";
        if (typeof t[2] != "string") {
        } else {
            shortFIO += t[2].charAt(0) + ".";
        }
    }
    return shortFIO
}
function loopSQL(sqlSection, execSection) {
    _$LOG(2, "loopSQL sqlSection = " + sqlSection + " execSection" + execSection + "\n");
    var sql = BT.getCustomSectionAsString(sqlSection);
    if (sql.length === 0) {
//        BT.WriteLog(2, "<font color=red> loopSQL:" + sqlSection + ": SECTION NOT FOUND OR EMPTY</font>");
        return;
    }
    var r = dbUtil.getResults(sql);
    if (typeof r == "undefined") {
        return;
    }
    if (r == null) {
        return;
    }
    var headers = getColNames(r);
    var sumstr = "";
    while (r.next()) {
        for (i = 0; i < headers.length; i++) {
            var val = r.getString(i + 1);
            BT.addParameter(headers[i], val);
            sumstr += headers[i] + " = [" + val + "] ";
        }
//        _$LOG(2, "<br>Current row= " + sumstr + "\n", sectionLines, out);
        BT.getCustomSection("", execSection, out);
    }
    dbUtil.closeResultSet(r);
}
;
// Возвращает  параметр  по имени namr
function prm(name) {
    return BT.getParameter(null, null, name);
}
// Устанавливает   параметр  по имени namr
function setPrm(name, val) {
    BT.addParameter(name, val);
}
function prmSet(name, val) {
    BT.addParameter(name, val);
}
function setSessPrm(name, val) {
    
    BT._$SET_PARAMETERS("$SET_PARAMETERS_SESSION " + name+"="+val, null, out, "setsession param");
}
// исполняется аналогично $INCLUDE 
function _$INCLUDE(sectionName) {
    BT._$INCLUDE("$INCLUDE " + sectionName, sectionLines, out, sectionName);
}
function _$GET_DATA(sectionName) {
    BT._$GET_DATA("$GET_DATA " + sectionName, sectionLines, out, sectionName);
}

function _$LOG(lvl, msg) {
    BT.WriteLog(+lvl, msg);
}
var wf_ver=""; 
//var wf_ver="_v2";
//        var personal_parameters ={};
//        function parsePersonalParameters(){
//            personal_parameters = prm('personal_parameters');
//            _$LOG(2,'<br>var personal parameters = '+personal_parameters+'<br>');
//            if( ( typeof personal_parameters === 'undefined') || (personal_parameters.length==0) ){
//                personal_parameters = '{}'  ;
//            }
//            personal_parameters = JSON.parse(personal_parameters);
//            _$LOG(2,'JSON personal parameters='+JSON.stringify(personal_parameters)+'<br>');
//            for (var el in personal_parameters){
//                var name = el;  
//                var value = JSON.stringify(personal_parameters[el]);
//                value=value.substr(1,value.length-2);
//                setSessPrm(name,value);
//                _$LOG(2,'pers param['+name+'] = '+value+'<br>');
//            }    
//       }
       
       
       
       
       
       
//       parsePersonalParameters();
//       function setPersonalParameter(paramName){
//            var prmval = prm(paramName);
//             _$LOG(2,'personal parameter name = '+paramName+" value=" +prmval+'<br>');
//            if ((typeof prmval === 'undefined') || (prmval.length==0)){
//               prmval = '';
//            } 
//             _$LOG(2,'personal parameter name = '+paramName+" value=" +prmval+'<br>');
//            personal_parameters[paramName]=prmval;
//            setSessPrm('personal_parameters',JSON.stringify(personal_parameters));
//             _$LOG(2,'personal parametrers = '+JSON.stringify(personal_parameters)+'<br>');
//            
//       }




