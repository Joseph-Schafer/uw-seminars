package server;

import static spark.Spark.*;
import spark.Filter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SparkServer {
  public static void main(String[] args) {


    get("/quit", (req, res) -> {stop(); return ""; });
    after((Filter) (request, response) -> {
      response.header("Access-Control-Allow-Origin", "*");
      response.header("Access-Control-Allow-Methods", "GET");
    });


  }
}
