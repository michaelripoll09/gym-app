package com.gymapp.goals

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.util.UUID

class InvalidProgressGoalException: RuntimeException()
class ProgressGoalAccessDeniedException: RuntimeException()
@Service class ProgressGoalsService(private val jdbc: JdbcTemplate) {
    fun list(user: UUID) = jdbc.query("select * from progress_goals where user_id=? order by created_at desc", { r,_ -> response(user, r.getObject("id", UUID::class.java), r.getString("goal_type"), r.getDouble("target_value"), r.getObject("target_date", LocalDate::class.java), r.getString("status"), r.getString("exercise_name"), r.getTimestamp("completed_at")?.toInstant()?.toString()) }, user)
    fun create(user: UUID, r: ProgressGoalRequest): ProgressGoalResponse { validate(r); val id=UUID.randomUUID(); jdbc.update("insert into progress_goals(id,user_id,goal_type,exercise_name,target_value,target_date) values(?,?,?,?,?,?)",id,user,r.type,r.exerciseName?.trim(),r.targetValue,r.targetDate); return response(user,id,r.type,r.targetValue,r.targetDate,"ACTIVE",r.exerciseName,null) }
    fun update(user: UUID,id: UUID,r: ProgressGoalRequest) { validate(r); if(jdbc.update("update progress_goals set goal_type=?,exercise_name=?,target_value=?,target_date=? where id=? and user_id=?",r.type,r.exerciseName?.trim(),r.targetValue,r.targetDate,id,user)!=1) throw ProgressGoalAccessDeniedException() }
    fun complete(user: UUID,id: UUID) { if(jdbc.update("update progress_goals set status='COMPLETED',completed_at=now() where id=? and user_id=?",id,user)!=1) throw ProgressGoalAccessDeniedException() }
    fun delete(user: UUID,id: UUID) { if(jdbc.update("delete from progress_goals where id=? and user_id=?",id,user)!=1) throw ProgressGoalAccessDeniedException() }
    private fun validate(r: ProgressGoalRequest) { if(r.type !in setOf("BODY_WEIGHT","EXERCISE_LOAD") || r.targetValue !in 1.0..1000.0 || r.targetDate?.isBefore(LocalDate.now()) == true || (r.type=="BODY_WEIGHT" && !r.exerciseName.isNullOrBlank()) || (r.type=="EXERCISE_LOAD" && r.exerciseName.isNullOrBlank())) throw InvalidProgressGoalException() }
    private fun response(user:UUID,id:UUID,type:String,target:Double,date:LocalDate?,status:String,exercise:String?,completed:String?):ProgressGoalResponse { val current=if(type=="BODY_WEIGHT") jdbc.query("select weight_kg from body_measurements where user_id=? order by recorded_on desc limit 1",{r,_->r.getDouble(1)},user).firstOrNull() else jdbc.query("select l.load_kg from workout_set_logs l join workout_sessions s on s.id=l.session_id join exercises e on e.id=l.exercise_id where s.user_id=? and e.name=? and l.load_kg is not null order by s.started_at desc limit 1",{r,_->r.getDouble(1)},user,exercise).firstOrNull(); return ProgressGoalResponse(id,type,target,date,status,current,exercise,completed) }
}
