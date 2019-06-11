package com.zy.data.lts.executor.model;

import com.zy.data.lts.core.entity.Task;

import java.nio.file.Path;
import java.util.Calendar;

/**
 * @author chenqingsong
 * @date 2019/4/9 17:12
 */
public class JobExecuteEvent {
    private int flowTaskId;
    private int taskId;
    private int flowId;
    private int jobId;
    private int shard = 0;
    private String jobType;

    /**
     * 脚本所在目录
     */
    private Path output;
    private String params;

    public JobExecuteEvent() {
    }

    public JobExecuteEvent(int flowTaskId, int taskId, int shard, Path output, String params) {
        this.flowTaskId = flowTaskId;
        this.taskId = taskId;
        this.shard = shard;
        this.output = output;
        this.params = params;
    }

    public JobExecuteEvent(Task task, Path output, String params, String jobType) {
        this.flowTaskId = task.getFlowTaskId();
        this.taskId = task.getTaskId();
        this.flowId = task.getFlowId();
        this.jobId = task.getJobId();
        this.params = params;
        this.jobType = jobType;
        this.output = output;
    }

    public int getFlowTaskId() {
        return flowTaskId;
    }

    public void setFlowTaskId(int flowTaskId) {
        this.flowTaskId = flowTaskId;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public int getShard() {
        return shard;
    }

    public void setShard(int shard) {
        this.shard = shard;
    }

    public Path getOutput() {
        return output;
    }

    public void setOutput(Path output) {
        this.output = output;
    }

    public String getParams() {
        return ParamMatcher.replaceAll(params);
        //return params;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public String getJobType() {
        return jobType;
    }

    public void setJobType(String jobType) {
        this.jobType = jobType;
    }

    public int getFlowId() {
        return flowId;
    }

    public void setFlowId(int flowId) {
        this.flowId = flowId;
    }

    public int getJobId() {
        return jobId;
    }

    public void setJobId(int jobId) {
        this.jobId = jobId;
    }

    public enum ParamMatcher {
        YEAR("#\\{year\\}") {
            @Override
            String replace(String source) {
                Calendar c = Calendar.getInstance();
                return source.replaceAll(match, String.valueOf(c.get(Calendar.YEAR)));
            }
        },

        MONTH("#\\{month\\}") {
            @Override
            String replace(String source) {
                Calendar c = Calendar.getInstance();
                int month = c.get(Calendar.MONTH);
                return source.replaceAll(match, month < 10 ? "0" + month : "" + month);
            }
        },
        DAY("#\\{day\\}") {
            @Override
            String replace(String source) {
                Calendar c = Calendar.getInstance();
                int day = c.get(Calendar.DAY_OF_MONTH);
                return source.replaceAll(match, day < 10 ? "0" + day : "" + day);
            }
        },

        LAST_DAY("#\\{last_day\\}") {
            @Override
            String replace(String source) {
                Calendar c = Calendar.getInstance();
                c.add(Calendar.DATE, -1);
                int day = c.get(Calendar.DAY_OF_MONTH);
                return source.replaceAll(match, day < 10 ? "0" + day : "" + day);
            }
        },

        HOUR("#\\{hour\\}") {
            @Override
            String replace(String source) {
                Calendar c = Calendar.getInstance();
                int hour = c.get(Calendar.HOUR_OF_DAY);
                return source.replaceAll(match, hour < 10 ? "0" + hour : "" + hour);
            }
        },

        LAST_HOUR("#\\{last_hour\\}") {
            @Override
            String replace(String source) {
                Calendar c = Calendar.getInstance();
                c.add(Calendar.HOUR_OF_DAY,-1);
                int hour = c.get(Calendar.HOUR_OF_DAY);
                return source.replaceAll(match, hour < 10 ? "0" + hour : "" + hour);
            }
        },
        MINUTE("#\\{minute\\}") {
            @Override
            String replace(String source) {
                Calendar c = Calendar.getInstance();
                int minute = c.get(Calendar.MINUTE);
                return source.replaceAll(match, minute < 10 ? "0" + minute : "" + minute);
            }
        };

        String match;

        ParamMatcher(String match) {
            this.match = match;
        }

        abstract String replace(String source);

        public static String replaceAll(String source) {
            String ret = source;
            ParamMatcher[] matchers = ParamMatcher.values();
            for (ParamMatcher matcher : matchers) {
                ret = matcher.replace(ret);
            }

            return ret;
        }

    }

    public static void main(String[] args) {
        String aa="test     #{year}-#{month}-#{last_day} #{last_hour}:#{minute}  #{year}-#{month}-#{day} #{hour}:#{minute}";
        System.out.println(ParamMatcher.replaceAll(aa));
    }
}
