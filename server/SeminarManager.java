package server;

import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.apache.commons.lang3.tuple.*;

public class SeminarManager {

  private static final Map<String, Pair<String, Boolean>> departmentsToUrls = new HashMap<>();

  /**
   * Gets all UW Seminars, in a list by department.
   * @return a list of all upcoming UW Seminars.
   * @throws IOException if unable to connect to the webpages.
   */
  public static List<Seminar> getSeminars() throws IOException {
    if(departmentsToUrls.isEmpty()) {
      initMap();
    }
    List<Seminar> seminarsByDept = new ArrayList<>();
    for(String department : departmentsToUrls.keySet()) {
        List<Seminar> seminars =
            Seminar.getSeminarsFromWebpage(department,
                departmentsToUrls.get(department).getLeft(),
                departmentsToUrls.get(department).getRight());
        seminarsByDept.addAll(seminars);
    }
    return seminarsByDept;
  }

  public static Map<String, String> getDepartmentUrls() {
    if(departmentsToUrls.isEmpty()) {
      initMap();
    }
    Map<String, String> departmentUrlMap = new HashMap<>();
    for(String department: departmentsToUrls.keySet()) {
      departmentUrlMap.put(department, departmentsToUrls.get(department).getLeft());
    }
    return departmentUrlMap;
  }

  private static void initMap() {
    departmentsToUrls.put("Anthropology", new ImmutablePair<String, Boolean>("https://anthropology" +
        ".washington" +
        ".edu/calendar?trumbaEmbed=headinglevel%3D2%26filter2%3D_410449_%26-index%26filterfield2" +
        "%3D30073", true));
    departmentsToUrls.put("Economics", new ImmutablePair<String, Boolean>("https://econ.washington" +
        ".edu/calendar?trumbaEmbed=headinglevel%3D2%26filter1%3D_410449_%26-index%26filterfield1" +
        "%3D30073", true));
    departmentsToUrls.put("Engineering", new ImmutablePair<String, Boolean>("https://www.engr" +
        ".washington.edu/calendar?trumbaEmbed=headinglevel%3D2%26filter1%3D_410449_%26-index" +
        "%26filterfield1%3D30073", true));
    departmentsToUrls.put("History", new ImmutablePair<String, Boolean>("https://history.washington" +
        ".edu/calendar?trumbaEmbed=headinglevel%3D2" +
        "%26filter1%3D_410449_%26-index%26filterfield1%3D30073", true));
    departmentsToUrls.put("Philosophy", new ImmutablePair<String, Boolean>("https://phil" +
        ".washington" +
        ".edu/calendar?trumbaEmbed=headinglevel" +
        "%3D2%26filter1%3D_410449_%26-index%26filterfield1%3D30073", true));
    departmentsToUrls.put("Sociology", new ImmutablePair<String, Boolean>("https://soc.washington" +
        ".edu/calendar?trumbaEmbed=headinglevel%3D2" +
        "%26filter1%3D_410449_%26-index%26filterfield1%3D30073", true));
    departmentsToUrls.put("Computer Science", new ImmutablePair<String, Boolean>("https://www.cs" +
        ".washington.edu/events/colloquia", false));
  }

  public static void main(String[] args) throws IOException {
    java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF);

    Gson gson = new Gson();
    PrintStream dataWriter = new PrintStream(new File("./data.json"));
    dataWriter.print("{ \"seminars\": ");
    dataWriter.print(gson.toJson(getSeminars()));
    dataWriter.print(", \"departments\": ");
    dataWriter.print(gson.toJson(getDepartmentUrls()));
    dataWriter.print("}");

  }
}
