<template>
  <el-dialog
    :title="`${coachName}的课表`"
    :visible.sync="dialogVisible"
    width="85%"
    :before-close="handleClose"
    class="coach-schedule-dialog"
  >
    <div v-if="dialogVisible">
      <ScheduleCalendar
        :user-type="'coach'"
        :custom-user-id="coachId"
        :show-header="true"
      />
    </div>
  </el-dialog>
</template>

<script>
import ScheduleCalendar from '@/views/course/ScheduleCalendar.vue'

export default {
  name: 'CoachScheduleDialog',
  components: {
    ScheduleCalendar
  },
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    coachId: {
      type: [String, Number],
      required: true
    },
    coachName: {
      type: String,
      default: '教练'
    }
  },
  computed: {
    dialogVisible: {
      get() {
        return this.visible
      },
      set(val) {
        this.$emit('update:visible', val)
      }
    }
  },
  methods: {
    handleClose() {
      this.$emit('update:visible', false)
    }
  }
}
</script>

<style scoped>
.coach-schedule-dialog {
  margin-top: 5vh;
}

.coach-schedule-dialog >>> .el-dialog__body {
  padding: 20px;
}
</style>