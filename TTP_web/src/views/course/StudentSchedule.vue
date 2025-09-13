<template>
  <div class="student-schedule">
    <div class="page-header">
      <h2>我的课表</h2>
      <p>查看您的所有课程安排</p>
    </div>
    
    <ScheduleCalendar user-type="student" />
  </div>
</template>

<script>
import ScheduleCalendar from './ScheduleCalendar.vue'

export default {
  name: 'StudentSchedule',
  components: {
    ScheduleCalendar
  }
}
</script>

<style scoped>
.student-schedule {
  padding: 20px;
}

.page-header {
  margin-bottom: 30px;
  text-align: center;
}

.page-header h2 {
  font-size: 28px;
  color: #333;
  margin-bottom: 10px;
}

.page-header p {
  font-size: 16px;
  color: #666;
}
</style>