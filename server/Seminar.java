package server;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.FrameWindow;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;


/**
 * Represents an individual seminar event, with a location, time, and description.
 */
public class Seminar {
  private static final String SEMINAR_DATA_CLASS = "twSimpleTableEventRow";
  private static final String DATE_ROW_CLASS = "";
  private static final String COLUMN_HEADERS_CLASS = "twSimpleTableHeadRow";
  private String department;
  private String location;
  private SeminarDateTime startTime;
  private String description;
  private String url;

  private Seminar(String location, LocalDateTime startTime, String description, String department
      , String url) {
    SeminarDateTime time = new SeminarDateTime(startTime);
    this.location = location;
    this.startTime = time;
    this.description = description;
    this.department = department;
    this.url = url;
  }

  public static List<Seminar> getSeminarsFromWebpage(String department, String url,
                                                     boolean isTrumbaWebpage) throws IOException {
    if (isTrumbaWebpage) {
      return getSeminarsFromTrumbaWebpage(department, url);
    } else {
      return getSeminarsFromCustomPage(department, url);
    }
  }

  private static List<Seminar> getSeminarsFromTrumbaWebpage(String department, String url)
        throws IOException {
    List<Seminar> seminars = new ArrayList<Seminar>();
    final WebClient webClient = new WebClient(BrowserVersion.CHROME);
    webClient.getOptions().setJavaScriptEnabled(true);
    final HtmlPage outerPage = webClient.getPage(url);
    webClient.waitForBackgroundJavaScript(10000);
    FrameWindow eventFrame = outerPage.getFrameByName("trumba.spud.1.iframe");
    HtmlPage tablePage = (HtmlPage) eventFrame.getEnclosedPage();
    DomNode tableData = tablePage.querySelector("table.twSimpleTableTable").querySelector("tbody");
    DomNodeList<DomNode> tableElements = tableData.getChildNodes();
    LocalDate date = null;

    for(DomNode element : tableElements) {
      String elementClass = ((Element) element).getAttribute("class");
      if (elementClass.equals(DATE_ROW_CLASS)) {
        date = parseDate(element);
      }else if (elementClass.contains(SEMINAR_DATA_CLASS)) { //Different departments use slightly
        // different styling for their events, so might not all exactly match.
        LocalTime time = parseTime(element.querySelector("span.twStartTime").asText().trim());
        DomNode locationNode = element.querySelector("a.twLocation");
        String location = null;
        if (locationNode != null) {
          location = locationNode.asText();
        }
        String description = element.querySelector("a.twDescription").asText();
        LocalDateTime seminarTime = LocalDateTime.of(date, time);
        seminars.add(new Seminar(location, seminarTime, description, department, url));
      } else if (elementClass.contains(COLUMN_HEADERS_CLASS)) {
        continue;
      } else {
        System.out.println(element.asText());
        System.out.println(element.asXml());
        throw new IllegalArgumentException("Incorrect CSS class: " + elementClass);
      }
    }
    return seminars;
  }

  private static List<Seminar> getSeminarsFromCustomPage(String department, String url) throws IOException{
    switch (department) {
      case "Computer Science" :
        return getSeminarsFromComputerScience(department, url);
      default:
        throw new IllegalArgumentException("Not a valid custom department: " + department);
    }
  }
  private static List<Seminar> getSeminarsFromComputerScience(String department, String url) throws IOException{
    final WebClient webClient = new WebClient(BrowserVersion.CHROME);
    webClient.getOptions().setThrowExceptionOnScriptError(false);
    webClient.getOptions().setJavaScriptEnabled(true);
    final HtmlPage outerPage = webClient.getPage(url);
    webClient.waitForBackgroundJavaScript(10000);
    DomNode table = outerPage.getElementById("upcoming");
    List<Seminar> seminars = new ArrayList<>();
    LocalDate date = null;
    LocalTime time = null;
    String description = null;
    String seminarRelativeUrl = null;
    String semLocation = null;
    for(DomNode element : table.getChildNodes()) {
      String elementClass = ((Element) element).getAttribute("class");
      if(elementClass.contains("talkinfo")) {
        //Start of new seminar info
        if (date != null && time != null && description != null && semLocation != null) {
          LocalDateTime dateTime = LocalDateTime.of(date, time);
          Seminar seminar = new Seminar(semLocation, dateTime, description, department,
              url);
          seminars.add(seminar);
        }
        date = null;
        time = null;
        semLocation = null;
        seminarRelativeUrl = null;
        description = element.asText();
      } else if (elementClass.contains("link")) {
        description = description + ": " + element.asText();
      } else if (elementClass.contains("indent")) {
        if (date == null) {
          date = parseDate(element);
        } else if (time == null) {
          String timeLocString = element.asText();
          String timeString = timeLocString.substring(0, timeLocString.indexOf(','));
          semLocation = timeLocString.substring(timeLocString.indexOf(',') + 1).trim();
          time = parseTime(timeString);

        }
      }
    }
    return seminars;
  }

  private static LocalDate parseDate(DomNode element) {
    String dateString = element.asText();
    dateString = dateString.substring(dateString.indexOf(",") + 2); //Remove day of week
    String monthString = "";
    int day = 0;
    monthString = dateString.substring(0, 3);
    day = Integer.parseInt(dateString.substring(dateString.indexOf(" ") + 1,
        dateString.indexOf(",")));
    int year = Integer.parseInt(dateString.substring(dateString.indexOf(",") + 2));
    Month month = monthFromString(monthString);
    return LocalDate.of(year, month, day);
  }

  private static LocalTime parseTime(String timeString) {
    int hour;
    int minute = 0;
    int colonIndex = timeString.indexOf(':');
    if (colonIndex >=0) {
      hour = Integer.parseInt(timeString.substring(0, colonIndex));
      minute = Integer.parseInt(timeString.substring(colonIndex + 1, colonIndex + 3));
    }else {
      hour = Integer.parseInt(timeString.substring(0, timeString.indexOf(" ")));
    }
    if ((timeString.contains("p.m.") || timeString.contains("pm")) && hour < 12) {
      hour += 12;
    }
    return LocalTime.of(hour, minute);
  }

  private static Month monthFromString(String monthString) {
    switch(monthString) {
      case "January":
      case "Jan":
        return Month.JANUARY;
      case "February":
      case "Feb":
        return Month.FEBRUARY;
      case "March":
      case "Mar":
        return Month.MARCH;
      case "April":
      case "Apr":
        return Month.APRIL;
      case "May":
        return Month.MAY;
      case "June":
      case "Jun":
        return Month.JUNE;
      case "July":
      case "Jul":
        return Month.JULY;
      case "August":
      case "Aug":
        return Month.AUGUST;
      case "September":
      case "Sep":
        return Month.SEPTEMBER;
      case "October":
      case "Oct":
        return Month.OCTOBER;
      case "November":
      case "Nov":
        return Month.NOVEMBER;
      case "December":
      case "Dec":
        return Month.DECEMBER;
      default:
        throw new IllegalArgumentException("Invalid Month abbreviation: " + monthString);
    }
  }

  private class SeminarDateTime {
    public long timestamp;
    public String display;

    public SeminarDateTime(LocalDateTime time) {
      this.timestamp = time.atZone(ZoneId.of("America/Los_Angeles")).toEpochSecond();
      this.display = DateTimeFormatter.ofPattern("hh:mm a, M/d/yyyy").format(time);
    }
  }
}
