/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
function addCell(celltxt, colspan = 1, rowspan = 1, cellType = "simple") {
    var sectName = cellType + " cell";
    prm.set("colspan", colspan);
    prm.set("rowspan", rowspan);
    prm.set("txt", celltxt);
    out.println("sect = " + sectName);
    BT.getCustomSection("", sectName, out);
}
r = dbUtil.getResults("select  cat, base, step, srt from grade_scale order by srt");
var cats = [];
var bases = [];
var steps = [];
while (r.next()) {
    cats.push(r.getString(1));
    bases.push(r.getString(2));
    steps.push(r.getString(3));
}
addCell("");
cats.forEach(function (item, i, arr) {
    addCell(item, 6);
    addCell(" ", 1, 1, "simple");
});
BT.getCustomSection("", "new row", out);
addCell("Уровень");
BT.getCustomSection("", "category header", out);
out.println("!!!!!!!!!!!!!!USER_PD = "+BT.getParameter(null,null,"USER_ID"));


