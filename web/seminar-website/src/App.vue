<template>
  <div id="app" class="bg-primary">
    <seminar-table id="seminarTable" ></seminar-table>
  </div>
</template>

<script>
import seminarTable from "./components/seminarTable";
export default {
  name: 'app',
  components: {
    seminarTable
  }
};
</script>

<style>
  #app {
    font-family: 'Avenir', Helvetica, Arial, sans-serif;
    -webkit-font-smoothing: antialiased;
    -moz-osx-font-smoothing: grayscale;
    text-align: center;
    color: #2c3e50;
    margin-top: 60px;
  }
</style>
