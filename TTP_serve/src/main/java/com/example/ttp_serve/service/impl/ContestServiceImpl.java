package com.example.ttp_serve.service.impl;

import com.example.ttp_serve.entity.*;
import com.example.ttp_serve.enums.*;
import com.example.ttp_serve.exception.BusinessException;
import com.example.ttp_serve.exception.ResourceNotFoundException;
import com.example.ttp_serve.repository.*;
import com.example.ttp_serve.service.ContestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContestServiceImpl implements ContestService {

    private final MonthlyContestRepository contestRepository;
    private final ContestEnrollmentRepository enrollmentRepository;
    private final ContestScheduleRepository scheduleRepository;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final CourtRepository courtRepository;

    @Override
    @Transactional
    public MonthlyContest createContest(MonthlyContest contest) {
        // 检查校区是否存在
        if (contest.getCampus() == null || contest.getCampus().getId() == null) {
            throw new BusinessException("必须指定校区");
        }

        // 检查日期是否有效
        if (contest.getContestDate() == null || contest.getContestDate().isBefore(LocalDate.now())) {
            throw new BusinessException("比赛日期无效");
        }

        // 检查同一校区同一日期是否已有月赛
        if (contestRepository.findByCampusIdAndContestDate(
                contest.getCampus().getId(), contest.getContestDate()).isPresent()) {
            throw new BusinessException("该校区在该日期已有月赛");
        }

        contest.setStatus(ContestStatus.UPCOMING);
        contest.setCreatedAt(LocalDateTime.now());

        return contestRepository.save(contest);
    }

    @Override
    @Transactional
    public MonthlyContest updateContest(Long id, MonthlyContest contest) {
        MonthlyContest existingContest = contestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("月赛ID '" + id + "' 不存在"));

        // 只能更新未开始的月赛
        if (existingContest.getStatus() != ContestStatus.UPCOMING) {
            throw new BusinessException("只能修改未开始的月赛");
        }

        // 更新允许修改的字段
        if (contest.getContestDate() != null) {
            existingContest.setContestDate(contest.getContestDate());
        }

        if (contest.getGroupType() != null) {
            existingContest.setGroupType(contest.getGroupType());
        }

        return contestRepository.save(existingContest);
    }

    @Override
    @Transactional
    public void deleteContest(Long id) {
        MonthlyContest contest = contestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("月赛ID '" + id + "' 不存在"));

        // 检查是否有报名记录
        if (enrollmentRepository.countByContestId(id) > 0) {
            throw new BusinessException("该月赛已有报名记录，无法删除");
        }

        contestRepository.delete(contest);
    }

    @Override
    public MonthlyContest getContest(Long id) {
        return contestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("月赛ID '" + id + "' 不存在"));
    }

    @Override
    public List<MonthlyContest> getContestsByCampus(Long campusId) {
        return contestRepository.findByCampusId(campusId);
    }

    @Override
    public List<MonthlyContest> getContestsByStatus(ContestStatus status) {
        return contestRepository.findByStatus(status);
    }

    @Override
    public List<MonthlyContest> getUpcomingContests() {
        LocalDate today = LocalDate.now();
        return contestRepository.findUpcomingContests(today);
    }

    @Override
    @Transactional
    public ContestEnrollment enrollContest(ContestEnrollment enrollment) {
        // 检查月赛是否存在
        MonthlyContest contest = contestRepository.findById(enrollment.getContest().getId())
                .orElseThrow(() -> new ResourceNotFoundException("月赛ID '" + enrollment.getContest().getId() + "' 不存在"));

        // 检查学员是否存在
        Student student = studentRepository.findById(enrollment.getStudent().getId())
                .orElseThrow(() -> new ResourceNotFoundException("学员ID '" + enrollment.getStudent().getId() + "' 不存在"));

        // 检查是否已报名
        if (enrollmentRepository.existsByContestIdAndStudentId(contest.getId(), student.getId())) {
            throw new BusinessException("您已经报名过该月赛");
        }

        // 检查月赛状态
        if (contest.getStatus() != ContestStatus.UPCOMING) {
            throw new BusinessException("该月赛已截止报名");
        }

        // 检查报名日期
        if (contest.getContestDate().isBefore(LocalDate.now().plusDays(1))) {
            throw new BusinessException("月赛开始前1天停止报名");
        }

        enrollment.setStatus(EnrollmentStatus.REGISTERED);
        enrollment.setCreatedAt(LocalDateTime.now());

        return enrollmentRepository.save(enrollment);
    }

    @Override
    @Transactional
    public ContestEnrollment updateEnrollmentStatus(Long id, EnrollmentStatus status) {
        ContestEnrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("报名记录ID '" + id + "' 不存在"));

        enrollment.setStatus(status);

        return enrollmentRepository.save(enrollment);
    }

    @Override
    public List<ContestEnrollment> getEnrollmentsByContest(Long contestId) {
        // 检查月赛是否存在
        if (!contestRepository.existsById(contestId)) {
            throw new ResourceNotFoundException("月赛ID '" + contestId + "' 不存在");
        }

        return enrollmentRepository.findByContestId(contestId);
    }

    @Override
    public List<ContestEnrollment> getEnrollmentsByStudent(Long studentId) {
        // 检查学员是否存在
        if (!studentRepository.existsById(studentId)) {
            throw new ResourceNotFoundException("学员ID '" + studentId + "' 不存在");
        }

        return enrollmentRepository.findByStudentId(studentId);
    }

    @Override
    public boolean isStudentEnrolled(Long contestId, Long studentId) {
        // 检查月赛是否存在
        if (!contestRepository.existsById(contestId)) {
            throw new ResourceNotFoundException("月赛ID '" + contestId + "' 不存在");
        }

        // 检查学员是否存在
        if (!studentRepository.existsById(studentId)) {
            throw new ResourceNotFoundException("学员ID '" + studentId + "' 不存在");
        }

        return enrollmentRepository.existsByContestIdAndStudentId(contestId, studentId);
    }

    @Override
    @Transactional
    public ContestSchedule createSchedule(ContestSchedule schedule) {
        // 检查月赛是否存在
        MonthlyContest contest = contestRepository.findById(schedule.getContest().getId())
                .orElseThrow(() -> new ResourceNotFoundException("月赛ID '" + schedule.getContest().getId() + "' 不存在"));

        // 检查选手1是否存在
        if (schedule.getPlayer1() != null) {
            Student player1 = studentRepository.findById(schedule.getPlayer1().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("选手1ID '" + schedule.getPlayer1().getId() + "' 不存在"));
        }

        // 检查选手2是否存在（如果有）
        if (schedule.getPlayer2() != null) {
            Student player2 = studentRepository.findById(schedule.getPlayer2().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("选手2ID '" + schedule.getPlayer2().getId() + "' 不存在"));
        }

        // 检查球台是否存在（如果有）
        if (schedule.getCourt() != null) {
            Court court = courtRepository.findById(schedule.getCourt().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("球台ID '" + schedule.getCourt().getId() + "' 不存在"));
        }

        schedule.setStatus(ContestScheduleStatus.PENDING);
        schedule.setCreatedAt(LocalDateTime.now());

        return scheduleRepository.save(schedule);
    }

    @Override
    @Transactional
    public ContestSchedule updateSchedule(Long id, ContestSchedule schedule) {
        ContestSchedule existingSchedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("比赛安排ID '" + id + "' 不存在"));

        // 更新允许修改的字段
        if (schedule.getCourt() != null) {
            existingSchedule.setCourt(schedule.getCourt());
        }

        if (schedule.getStartTime() != null) {
            existingSchedule.setStartTime(schedule.getStartTime());
        }

        if (schedule.getStatus() != null) {
            existingSchedule.setStatus(schedule.getStatus());
        }

        if (schedule.getWinner() != null) {
            existingSchedule.setWinner(schedule.getWinner());
        }

        return scheduleRepository.save(existingSchedule);
    }

    @Override
    public List<ContestSchedule> getSchedulesByContest(Long contestId) {
        // 检查月赛是否存在
        if (!contestRepository.existsById(contestId)) {
            throw new ResourceNotFoundException("月赛ID '" + contestId + "' 不存在");
        }

        return scheduleRepository.findByContestId(contestId);
    }

    @Override
    public List<ContestSchedule> getSchedulesByRound(Long contestId, Integer roundNumber) {
        // 检查月赛是否存在
        if (!contestRepository.existsById(contestId)) {
            throw new ResourceNotFoundException("月赛ID '" + contestId + "' 不存在");
        }

        return scheduleRepository.findByContestIdAndRoundNumber(contestId, roundNumber);
    }

    @Override
    public List<ContestSchedule> getSchedulesByPlayer(Long playerId) {
        // 检查学员是否存在
        if (!studentRepository.existsById(playerId)) {
            throw new ResourceNotFoundException("学员ID '" + playerId + "' 不存在");
        }

        return scheduleRepository.findByPlayerId(playerId);
    }

    @Override
    @Transactional
    public void generateContestSchedule(Long contestId, ContestGroup groupType) {
        MonthlyContest contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new ResourceNotFoundException("月赛ID '" + contestId + "' 不存在"));

        // 获取该组别的报名学员
        List<ContestEnrollment> enrollments = enrollmentRepository.findByContestIdAndGroupType(contestId, groupType);

        if (enrollments.isEmpty()) {
            throw new BusinessException("该组别没有报名学员");
        }

        List<Student> players = enrollments.stream()
                .map(ContestEnrollment::getStudent)
                .collect(Collectors.toList());

        // 获取可用球台
        List<Court> courts = courtRepository.findByCampusIdAndStatus(contest.getCampus().getId(), CourtStatus.AVAILABLE);

        if (courts.isEmpty()) {
            throw new BusinessException("没有可用球台");
        }

        // 根据人数确定赛制
        if (players.size() <= 6) {
            // 6人及以下采用循环赛
            generateRoundRobinSchedule(contest, groupType, players, courts);
        } else {
            // 7人及以上采用小组赛+淘汰赛
            generateGroupStageSchedule(contest, groupType, players, courts);
        }

        // 更新月赛状态
        contest.setStatus(ContestStatus.ONGOING);
        contestRepository.save(contest);
    }

    /**
     * 生成循环赛赛程
     */
    private void generateRoundRobinSchedule(MonthlyContest contest, ContestGroup groupType,
                                            List<Student> players, List<Court> courts) {
        int numPlayers = players.size();
        int numRounds = numPlayers % 2 == 0 ? numPlayers - 1 : numPlayers;

        // 随机打乱球员顺序
        Collections.shuffle(players);

        for (int round = 1; round <= numRounds; round++) {
            for (int i = 0; i < numPlayers / 2; i++) {
                Student player1 = players.get(i);
                Student player2 = players.get(numPlayers - 1 - i);

                // 创建比赛安排
                ContestSchedule schedule = new ContestSchedule();
                schedule.setContest(contest);
                schedule.setRoundNumber(round);
                schedule.setPlayer1(player1);
                schedule.setPlayer2(player2);
                schedule.setCourt(getAvailableCourt(courts, contest.getContestDate().atTime(9, 0).plusHours(round - 1)));
                schedule.setStartTime(contest.getContestDate().atTime(9, 0).plusHours(round - 1));
                schedule.setStatus(ContestScheduleStatus.PENDING);
                schedule.setCreatedAt(LocalDateTime.now());

                scheduleRepository.save(schedule);
            }

            // 旋转球员列表（固定第一个球员，旋转其他球员）
            if (numPlayers > 2) {
                Student first = players.get(0);
                Student second = players.get(1);
                players.remove(0);
                players.remove(0);
                players.add(second);
                players.add(0, first);
            }
        }
    }

    /**
     * 生成小组赛+淘汰赛赛程
     */
    private void generateGroupStageSchedule(MonthlyContest contest, ContestGroup groupType,
                                            List<Student> players, List<Court> courts) {
        int numPlayers = players.size();
        int numGroups = (int) Math.ceil(numPlayers / 4.0); // 每组约4人

        // 随机打乱球员顺序
        Collections.shuffle(players);

        // 分组
        List<List<Student>> groups = new ArrayList<>();
        for (int i = 0; i < numGroups; i++) {
            groups.add(new ArrayList<>());
        }

        for (int i = 0; i < numPlayers; i++) {
            groups.get(i % numGroups).add(players.get(i));
        }

        // 生成小组赛
        int round = 1;
        for (int groupIndex = 0; groupIndex < groups.size(); groupIndex++) {
            List<Student> groupPlayers = groups.get(groupIndex);

            // 小组内循环赛
            for (int i = 0; i < groupPlayers.size(); i++) {
                for (int j = i + 1; j < groupPlayers.size(); j++) {
                    Student player1 = groupPlayers.get(i);
                    Student player2 = groupPlayers.get(j);

                    ContestSchedule schedule = new ContestSchedule();
                    schedule.setContest(contest);
                    schedule.setRoundNumber(round);
                    schedule.setPlayer1(player1);
                    schedule.setPlayer2(player2);
                    schedule.setCourt(getAvailableCourt(courts, contest.getContestDate().atTime(9, 0).plusHours(round - 1)));
                    schedule.setStartTime(contest.getContestDate().atTime(9, 0).plusHours(round - 1));
                    schedule.setStatus(ContestScheduleStatus.PENDING);
                    schedule.setCreatedAt(LocalDateTime.now());

                    scheduleRepository.save(schedule);
                    round++;
                }
            }
        }

        // TODO: 生成淘汰赛赛程（根据小组赛结果）
        // 这里需要先进行小组赛，然后根据小组排名生成淘汰赛
    }

    /**
     * 获取可用球台
     */
    private Court getAvailableCourt(List<Court> courts, LocalDateTime startTime) {
        // 简单实现：随机选择一个球台
        // 实际应用中应该检查球台在该时间段是否可用
        Random random = new Random();
        return courts.get(random.nextInt(courts.size()));
    }

    @Override
    public Long countEnrollmentsByContest(Long contestId) {
        // 检查月赛是否存在
        if (!contestRepository.existsById(contestId)) {
            throw new ResourceNotFoundException("月赛ID '" + contestId + "' 不存在");
        }

        return enrollmentRepository.countByContestId(contestId);
    }

    @Override
    public Long countEnrollmentsByGroup(Long contestId, ContestGroup groupType) {
        // 检查月赛是否存在
        if (!contestRepository.existsById(contestId)) {
            throw new ResourceNotFoundException("月赛ID '" + contestId + "' 不存在");
        }

        return enrollmentRepository.countByContestIdAndGroupType(contestId, groupType);
    }

    @Override
    @Transactional
    public ContestSchedule updateMatchResult(Long scheduleId, Long winnerId) {
        ContestSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("比赛安排ID '" + scheduleId + "' 不存在"));

        // 检查选手是否存在
        Student winner = studentRepository.findById(winnerId)
                .orElseThrow(() -> new ResourceNotFoundException("选手ID '" + winnerId + "' 不存在"));

        // 检查获胜者是否是比赛选手
        if (!winnerId.equals(schedule.getPlayer1().getId()) &&
                !winnerId.equals(schedule.getPlayer2().getId())) {
            throw new BusinessException("获胜者必须是比赛选手之一");
        }

        schedule.setWinner(winner);
        schedule.setStatus(ContestScheduleStatus.COMPLETED);

        return scheduleRepository.save(schedule);
    }
}