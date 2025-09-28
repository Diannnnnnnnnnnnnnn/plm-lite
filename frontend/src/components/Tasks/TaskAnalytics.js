import React from 'react';
import {
  Box,
  Typography,
  Grid,
  Card,
  CardContent,
  Paper,
  List,
  ListItem,
  ListItemText,
  ListItemIcon,
  Avatar,
  Chip,
  LinearProgress
} from '@mui/material';
import {
  Assignment as TaskIcon,
  TrendingUp as TrendingUpIcon,
  Schedule as ScheduleIcon,
  CheckCircle as CompletedIcon,
  Warning as OverdueIcon,
  Person as PersonIcon
} from '@mui/icons-material';

const StatCard = ({ title, value, subtitle, icon, color = 'primary', trend }) => (
  <Card sx={{ height: '100%' }}>
    <CardContent>
      <Box display="flex" alignItems="center" justifyContent="space-between">
        <Box>
          <Typography color="textSecondary" gutterBottom variant="h6">
            {title}
          </Typography>
          <Typography variant="h4" component="h2">
            {value}
          </Typography>
          {subtitle && (
            <Typography variant="body2" color="textSecondary">
              {subtitle}
            </Typography>
          )}
          {trend && (
            <Box display="flex" alignItems="center" mt={1}>
              <TrendingUpIcon fontSize="small" color="success" />
              <Typography variant="body2" color="success.main" sx={{ ml: 0.5 }}>
                {trend}
              </Typography>
            </Box>
          )}
        </Box>
        <Box color={`${color}.main`}>
          {icon}
        </Box>
      </Box>
    </CardContent>
  </Card>
);

const TaskAnalytics = () => {
  const taskStats = {
    total: 45,
    completed: 28,
    inProgress: 12,
    overdue: 5,
    completionRate: 62
  };

  const topPerformers = [
    { name: 'John Doe', avatar: 'JD', completed: 12, total: 15 },
    { name: 'Jane Smith', avatar: 'JS', completed: 8, total: 10 },
    { name: 'Mike Johnson', avatar: 'MJ', completed: 6, total: 8 },
    { name: 'Sarah Wilson', avatar: 'SW', completed: 4, total: 6 }
  ];

  const recentActivities = [
    { task: 'Design Review Completed', user: 'John Doe', time: '2 hours ago', status: 'completed' },
    { task: 'BOM Validation Started', user: 'Jane Smith', time: '4 hours ago', status: 'started' },
    { task: 'Documentation Updated', user: 'Mike Johnson', time: '1 day ago', status: 'updated' },
    { task: 'Testing Phase Completed', user: 'Sarah Wilson', time: '2 days ago', status: 'completed' }
  ];

  const tasksByCategory = [
    { category: 'Design', count: 15, color: 'primary' },
    { category: 'Engineering', count: 12, color: 'secondary' },
    { category: 'Documentation', count: 8, color: 'warning' },
    { category: 'Testing', count: 6, color: 'success' },
    { category: 'Other', count: 4, color: 'info' }
  ];

  const getStatusColor = (status) => {
    switch (status) {
      case 'completed': return 'success';
      case 'started': return 'primary';
      case 'updated': return 'info';
      default: return 'default';
    }
  };

  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        Task Analytics
      </Typography>

      {/* Statistics Cards */}
      <Grid container spacing={3} sx={{ mb: 4 }}>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Total Tasks"
            value={taskStats.total}
            icon={<TaskIcon fontSize="large" />}
            color="primary"
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Completed"
            value={taskStats.completed}
            subtitle={`${taskStats.completionRate}% completion rate`}
            icon={<CompletedIcon fontSize="large" />}
            color="success"
            trend="+12% this week"
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="In Progress"
            value={taskStats.inProgress}
            icon={<ScheduleIcon fontSize="large" />}
            color="warning"
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Overdue"
            value={taskStats.overdue}
            subtitle="Needs attention"
            icon={<OverdueIcon fontSize="large" />}
            color="error"
          />
        </Grid>
      </Grid>

      <Grid container spacing={3}>
        {/* Tasks by Category */}
        <Grid item xs={12} md={6}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              Tasks by Category
            </Typography>
            <List>
              {tasksByCategory.map((item) => (
                <ListItem key={item.category}>
                  <ListItemText
                    primary={item.category}
                    secondary={
                      <Box>
                        <Box display="flex" justifyContent="space-between" alignItems="center" mb={1}>
                          <Typography variant="body2">{item.count} tasks</Typography>
                          <Typography variant="body2">
                            {Math.round((item.count / taskStats.total) * 100)}%
                          </Typography>
                        </Box>
                        <LinearProgress
                          variant="determinate"
                          value={(item.count / taskStats.total) * 100}
                          color={item.color}
                          sx={{ height: 6, borderRadius: 3 }}
                        />
                      </Box>
                    }
                  />
                </ListItem>
              ))}
            </List>
          </Paper>
        </Grid>

        {/* Top Performers */}
        <Grid item xs={12} md={6}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              Top Performers
            </Typography>
            <List>
              {topPerformers.map((performer, index) => (
                <ListItem key={performer.name}>
                  <ListItemIcon>
                    <Avatar sx={{ bgcolor: 'primary.main' }}>
                      {performer.avatar}
                    </Avatar>
                  </ListItemIcon>
                  <ListItemText
                    primary={performer.name}
                    secondary={
                      <Box>
                        <Typography variant="body2" color="textSecondary">
                          {performer.completed}/{performer.total} tasks completed
                        </Typography>
                        <LinearProgress
                          variant="determinate"
                          value={(performer.completed / performer.total) * 100}
                          sx={{ mt: 1, height: 4, borderRadius: 2 }}
                        />
                      </Box>
                    }
                  />
                  <Chip
                    label={`#${index + 1}`}
                    size="small"
                    color="primary"
                    variant="outlined"
                  />
                </ListItem>
              ))}
            </List>
          </Paper>
        </Grid>

        {/* Recent Activities */}
        <Grid item xs={12}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              Recent Activities
            </Typography>
            <List>
              {recentActivities.map((activity, index) => (
                <ListItem key={index}>
                  <ListItemIcon>
                    <Avatar sx={{ bgcolor: 'grey.100' }}>
                      <PersonIcon color="action" />
                    </Avatar>
                  </ListItemIcon>
                  <ListItemText
                    primary={
                      <Box display="flex" alignItems="center" gap={1}>
                        <Typography variant="body1">{activity.task}</Typography>
                        <Chip
                          label={activity.status}
                          size="small"
                          color={getStatusColor(activity.status)}
                        />
                      </Box>
                    }
                    secondary={`${activity.user} • ${activity.time}`}
                  />
                </ListItem>
              ))}
            </List>
          </Paper>
        </Grid>
      </Grid>
    </Box>
  );
};

export default TaskAnalytics;