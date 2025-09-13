<template>
  <div class="coach-schedule">
    <div class="page-header">
      <h2>教练课表</h2>
      <p>查看您的所有课程安排</p>
    </div>
    
    <ScheduleCalendar user-type="coach" />
  </div>
</template>

<script>
import ScheduleCalendar from './ScheduleCalendar.vue'

export default {
  name: 'CoachSchedule',
  components: {
    ScheduleCalendar
  }
}
</script>

<style scoped>
.coach-schedule {
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