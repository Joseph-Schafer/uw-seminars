<template>
    <div class="container">
        <h1 class="text-white" id="heading">UW Seminar Database</h1>
        <p class="text-secondary">A personal project by Joseph Schafer</p>
        <div class="mb-3 d-block">
            <button type="button"
                    class="btn btn-primary mb-3 btn-outline-secondary float-left"
                    data-toggle="collapse"
                    data-target="#filterOptions">
                Filter
            </button>
            <button type="button"
                    class="btn btn-primary mb-3 btn-outline-secondary float-right"
                    data-toggle="collapse"
                    data-target="#links">
                Department Pages
            </button>
            <div class="collapse clear-after-float" id="filterOptions">
                <div class="card">
                    <h5 class="card-title text-primary mt-2">Departments</h5>
                    <div class="card-body">
                        <div class="row">
                            <div v-for="department in departments"
                                v-bind:key="department.name" class="col-xs-12 col-sm-6 col-md-4 col-lg-3">
                                <input type="checkbox" v-bind:id="department.name" v-model="department.enabled">
                                <label v-bind:for="department.name" class="text-primary"> &nbsp;{{department.name}}</label>
                            </div>
                        </div>
                        <button type="button"
                                class="btn btn-secondary mt-3 float-left"
                                data-toggle="collapse"
                                data-target="#filterOptions">
                            Close
                        </button>
                    </div>
                </div>
            </div>
            <div class="collapse clear-after-float" id="links">
                <div class="card">
                    <h5 class="card-title text-primary mt-2">Department Pages</h5>
                    <div class="card-body">
                        <div class="row">
                            <div v-for="department in departments"
                                 v-bind:key="department.name" class="col-xs-12 col-sm-6 col-md-4 col-lg-3">
                                <a v-bind:href="department.url" class="text-primary">{{department.name}}</a>
                            </div>
                        </div>
                        <button type="button"
                                class="btn btn-secondary mt-3 float-left"
                                data-toggle="collapse"
                                data-target="#filterOptions">
                            Close
                        </button>
                    </div>
                </div>
            </div>
        </div>
        <div v-for="seminar in seminars"
            class="card mb-3 clear-after-float"
            :key="seminar.description + seminar.department + seminar.startTime.timestamp">
            <div class="card-body">
                <div class="card-title text-primary">
                    {{seminar.description}}
                </div>
                <div class="card-subtitle text-muted">
                    {{seminar.startTime.display}}{{seminar.location ? ", " + seminar.location : ""}}
                    <br>
                    {{seminar.department}}
                </div>
                <div class="card-text text-primary">
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="js">
    import json from "../assets/data";

    export default {
        name: "seminarTable.vue",
        data() {
          return {
              departments: []
          };
        },
        computed: {
            initialDepartments()  {
              let departmentsData = [];
              for (let key in json.departments) {
                  if(json.departments.hasOwnProperty(key)) {
                      departmentsData.push({url: json.departments[key], name: key, enabled: true});
                  }
              }
              departmentsData.sort((a, b) => {
                  if (a.name < b.name) {
                      return -1;
                  } if (a.name > b.name) {
                      return 1;
                  }
                  return 0;
              });
              return departmentsData;
            },
            seminars()  {
                let enabledDepartments = [];
                for(let index in this.departments) {
                    if (this.departments[index].enabled) {
                        enabledDepartments.push(this.departments[index].name);
                    }
                }
                let seminarDataArray = [];
                for (let seminar in json.seminars) {
                    if (json.seminars.hasOwnProperty(seminar) && enabledDepartments.includes(json.seminars[seminar].department)) {
                        seminarDataArray.push(json.seminars[seminar]);
                    }
                }
                seminarDataArray.sort(function (a, b) {
                    return a.startTime.timestamp - b.startTime.timestamp;
                });
                return seminarDataArray;
            }
        },
        methods: {
        },
        created() {
            this.departments = this.initialDepartments;
        }
    }
</script>

<style scoped>
    .clear-after-float {
        clear: both;
    }
</style>