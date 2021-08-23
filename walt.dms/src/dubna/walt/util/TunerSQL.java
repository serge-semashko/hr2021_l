package dubna.walt.util;

/**
 *  The class for correction of the SQL created by Tuner.
 *
 *  <br>The SQL must be a String array.
 */

public class TunerSQL
{

/**
 * Deletes from the SQL superfluous tokens,
 * which may remain after the SQL tuning.
 *
 * <P><b>Deletes:</b><br>
 * - the first comma in the 2nd line if the comma is at the first position;<BR>
 * - commas in the last line;<BR>
 * - the first "and" after "where" if "and" appears as the first token of the following line.<BR>
 * <p><b>
 * Replaces:</b><br> " ` " by " ' ".
 *
 * @param sql the String array containing the SQL to be corrected.
 *
 */
static public void cleanSQL(String[] sql)
{
//  System.out.println("== cleanSQL -==========");
  int numLines=sql.length;
  int i;

  for (i=0; i < sql.length; i++)
    sql[i] = sql[i].trim();
    
  if (numLines > 2)
  { if (sql[0].indexOf(" ") < 0 && sql[1].indexOf(",") == 0)
    {   sql[1]=sql[1].substring(1);    // the first comma in the 2nd line
    }
    if (sql[1].indexOf("/*+") == 0)      // the hint
    {   if (sql[2].indexOf(",") == 0) sql[2]=sql[2].substring(1);    // the first comma in the 2nd line
    }

    if (sql[numLines-1].endsWith(",")) sql[numLines-1]=sql[numLines-1].substring(0,sql[numLines-1].length()-1);
  }

  /*  Убрано 03.03.2020. Непонятно, зачем была нужна возможность возвращать в запрос прямую кавычку.
  for (i=0; (i<numLines) && (sql[i] != null); i++)
  {
    sql[i]=StrUtil.replaceInString(sql[i],"/`","'");
//    sql[i]=StrUtil.replaceInString(sql[i],"`","''");
  }
*/
  
  i = StrUtil.indexOfToken(sql,"WHERE",1);
  if (i < 1) return;

  correctInClause(sql);
  like2OrLike(sql);
  correctHavingClause(sql);

  //======= Delete the first "and" after "WHERE",
  //======= if it is the first token after "WHERE"
  // !!! there is a problem - this code looks for the first occurence of WHERE only!
  if (sql[i].trim().length() == 5)  // there are no more tokens after "WHERE" in the same line
  {
    for (i++; (i<numLines) && (sql[i] != null); i++)
    {
//        System.out.println("== line " + i + ": '" + sql[i] + "'");
      if (sql[i].toUpperCase().indexOf("AND ") == 0)  //The first token after "where" is "and "
      {
        sql[i]=sql[i].substring(3);       // - delete it!
        break;
      }
      if (sql[i].trim().length() > 0) break;  //the first token after "where" is not "and" - exit!
    }
  }


  //======= Delete the first comma after "GROUP BY",
  //======= if it is the first token after "GROUP BY"
  i = StrUtil.indexOfToken(sql,"GROUP BY",i);
  for (i++; (i<numLines) && (sql[i] != null); i++)
  {
    if (sql[i].indexOf(",") == 0)  //The first charachter after "group by" is comma
    {
      sql[i]=sql[i].substring(1);  // - delete it!
      break;
    }
    if (sql[i].trim().length() > 0) break;  //the first token after "where" is not comma - exit!
  }
  correctSpaces (sql);
}

/**
 * Corrects spaces.
 *
 * @param sql the String array containing the SQL to be corrected.
 *
 */
static public void correctSpaces (String[] sql)
{
  int i = StrUtil.indexOfToken(sql,"WHERE",1);
  for (i++; (i<sql.length) && (sql[i] != null); i++)
  {
    sql[i] = sql[i].replace(' ',' ');
  }
}

/**
 * Corrects the "HAVING" criteria.
 *
 * <p>Finds two sequential lines conteining criteria<br><b>
 * "HAVING ...",</b><p>
 * and converts it into:<br><b>
 * "HAVING (... and ...)"</b>
 *
 * @param sql the String array containing the SQL to be corrected.
 *
 */
static public void correctHavingClause (String[] sql)
{
  int i = StrUtil.indexOfToken(sql,"HAVING", 2);
  if ( i < 0) return;
  int j = StrUtil.indexOfToken(sql,"HAVING", i + 1);
  if ( j < 0) return;
  sql[i] = "HAVING (" + sql[i].substring(7)
         + " and " + sql[j].substring(7) + ")";
  sql[j] = " ";
}

/**
 * Corrects the "IN" criteria.
 *
 * <p>Finds the criteria:<br><b>
 * "And Field IN() 'Val1','Val2',...",</b><p>
 * and converts it into:<br><b>
 * "And Field IN ('Val1','Val2' ...)"</b>
 *
 * @param sql the String array containing the SQL to be corrected.
 *
 */
static public void correctInClause (String[] sql)
{

//  System.out.println("correctInClause: " );
  int i = 1;
  boolean needToCorrect = false;
  while ( (i = StrUtil.indexOfToken(sql, "IN()", i+1) ) > 0)
  { sql[i] = StrUtil.replaceInString(sql[i], "IN()", "IN(" )
            + ")";
    needToCorrect = true;
  }
  if (!needToCorrect) return;
  
  i = 1;
  while ( (i = StrUtil.indexOfToken(sql, "IN('", i+1) ) > 0)
  { sql[i] = StrUtil.replaceInString(sql[i], ",", "','" );
    sql[i] = StrUtil.replaceInString(sql[i], "',' ", "','" );
    sql[i] = StrUtil.replaceInString(sql[i], "'',''", "','" );

    if (sql[i].indexOf("%") > 0)
      sql[i] = StrUtil.replaceInString(sql[i], "in(", "like (" );
  }
}

/**
 * Corrects the "LIKE" criteria.
 *
 * <p>Finds the criteria:<br><b>
 * "And Field Like 'Val1','Val2',...",</b><p>
 * and converts it into:<br><b>
 * "And (Field Like 'Val1' OR Field Like 'Val2' ...)"</b>
 *
 * @param sql the String array containing the SQL to be corrected.
 *
 */
static public void like2OrLike (String[] sql)
{
  int i, likePos, commaPos;
  StringBuilder result;
  String field, s, value;

  i = StrUtil.indexOfToken(sql,"WHERE",1);
  if (i < 0) return;

  while ( ++i < sql.length)
  {
    s=sql[i];
    likePos=s.indexOf(" LIKE ");    // find the index of the "LIKE" keyword
//    likePos=s.toUpperCase().indexOf(" LIKE ");    // 28.10.2005 - "Upper" removed to be able to avoid processing
    commaPos=s.indexOf(",");          // find the index of the comma
    if (likePos > 0 && commaPos > likePos)  // both present - processing needed!
    {
      result = new StringBuilder ("and ");
      if (s.toUpperCase().indexOf(" NOT ") > 0) // it is the "NOT LIKE" -
        result.append("not ");      // add "NOT" to the criteria

      result.append("(");
      field = StrUtil.getWord(s, 2, ' ');   // the database field name
      s=s.substring(likePos+5).trim();  // the list of values
      int valNr=1;

      //===== loop trough the values =====
      while ((value=StrUtil.getWord(s, valNr++,',')).length() > 0)
      {
        if (value.charAt(0) != '\'' && value.charAt(1) != '\'')
          value = "'" + value;
        if (value.charAt(value.length()-1) != '\'' && value.charAt(value.length()-2) != '\'')
          value += "'";
        result.append("(" + field + " like " + value + ") OR ");
      }

      //===== final - convert the Vector to String
      //===== and delete the last (superfluos) " OR "
      s=result.toString();
      s=s.substring(0,s.length()-4).concat(")");
      sql[i]=s;
    }
  }
}



}