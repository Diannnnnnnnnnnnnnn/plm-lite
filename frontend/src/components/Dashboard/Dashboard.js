import React, { useState, useEffect } from 'react';
import {
  Grid,
  Card,
  CardContent,
  Typography,
  Box,
  Paper,
  List,
  ListItem,
  ListItemText,
  ListItemIcon,
  Chip,
  CircularProgress
} from '@mui/material';
import {
  Description as DocumentIcon,
  AccountTree as BOMIcon,
  Assignment as TaskIcon,
  ChangeCircle as ChangeIcon,
  TrendingUp as TrendingUpIcon
} from '@mui/icons-material';
import documentService from '../../services/documentService';
import bomService from '../../services/bomService';
import taskService from '../../services/taskService';
import changeService from '../../services/changeService';

const StatCard = ({ title, value, icon, color = 'primary', loading = false }) => (
  <Card sx={{ height: '100%' }}>
    <CardContent>
      <Box display="flex" alignItems="center" justifyContent="space-between">
        <Box>
          <Typography color="textSecondary" gutterBottom variant="h6">
            {title}
          </Typography>
          <Typography variant="h4" component="h2">
            {loading ? <CircularProgress size={32} /> : value}
          </Typography>
        </Box>
        <Box color={`${color}.main`}>
          {icon}
        </Box>
      </Box>
    </CardContent>
  </Card>
);

const getActivityIcon = (type) => {
  switch (type) {
    case 'document': return <DocumentIcon />;
    case 'bom': return <BOMIcon />;
    case 'task': return <TaskIcon />;
    case 'change': return <ChangeIcon />;
    default: return <DocumentIcon />;
  }
};

const getStatusColor = (status) => {
  switch (status?.toUpperCase()) {
    case 'DRAFT': return 'default';
    case 'IN_WORK': return 'info';
    case 'IN_REVIEW': return 'warning';
    case 'RELEASED': return 'success';
    case 'APPROVED': return 'success';
    case 'COMPLETED': return 'success';
    case 'PENDING': return 'warning';
    default: return 'default';
  }
};

const formatTimeAgo = (dateString) => {
  if (!dateString) return 'Recently';

  const date = new Date(dateString);
  const now = new Date();
  const diffMs = now - date;
  const diffMins = Math.floor(diffMs / 60000);
  const diffHours = Math.floor(diffMs / 3600000);
  const diffDays = Math.floor(diffMs / 86400000);

  if (diffMins < 60) return `${diffMins} min${diffMins !== 1 ? 's' : ''} ago`;
  if (diffHours < 24) return `${diffHours} hour${diffHours !== 1 ? 's' : ''} ago`;
  return `${diffDays} day${diffDays !== 1 ? 's' : ''} ago`;
};

export default function Dashboard() {
  const [stats, setStats] = useState({
    documents: 0,
    boms: 0,
    tasks: 0,
    changes: 0
  });
  const [loading, setLoading] = useState(true);
  const [recentActivities, setRecentActivities] = useState([]);

  useEffect(() => {
    loadDashboardData();
  }, []);

  const loadDashboardData = async () => {
    try {
      setLoading(true);

      // Fetch data from all services in parallel
      const [documents, boms, tasks, changes] = await Promise.all([
        documentService.getAllDocuments().catch(() => []),
        bomService.getAllBoms().catch(() => []),
        taskService.getAllTasks().catch(() => []),
        changeService.getAllChanges().catch(() => [])
      ]);

      // Update statistics
      setStats({
        documents: documents.length || 0,
        boms: boms.length || 0,
        tasks: tasks.filter(t => t.status !== 'COMPLETED').length || 0,
        changes: changes.length || 0
      });

      // Build recent activities list
      const activities = [];

      // Add recent documents (last 3)
      documents.slice(0, 3).forEach(doc => {
        activities.push({
          id: `doc-${doc.id}`,
          type: 'document',
          title: `${doc.masterId} ${doc.version} - ${doc.status}`,
          time: formatTimeAgo(doc.createTime),
          status: doc.status
        });
      });

      // Add recent BOMs (last 2)
      boms.slice(0, 2).forEach(bom => {
        activities.push({
          id: `bom-${bom.id}`,
          type: 'bom',
          title: `BOM ${bom.partNumber} - ${bom.status}`,
          time: formatTimeAgo(bom.createdAt),
          status: bom.status
        });
      });

      // Add recent tasks (last 2)
      tasks.slice(0, 2).forEach(task => {
        activities.push({
          id: `task-${task.id}`,
          type: 'task',
          title: task.name,
          time: formatTimeAgo(task.createdAt),
          status: task.status
        });
      });

      // Add recent changes (last 2)
      changes.slice(0, 2).forEach(change => {
        activities.push({
          id: `change-${change.id}`,
          type: 'change',
          title: `${change.title} - ${change.status}`,
          time: formatTimeAgo(change.createTime),
          status: change.status
        });
      });

      // Sort by most recent
      setRecentActivities(activities.slice(0, 8));

    } catch (error) {
      console.error('Error loading dashboard data:', error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Box sx={{ height: 'calc(100vh - 120px)', overflow: 'auto', pr: 2 }}>
      {/* Statistics Cards */}
      <Grid container spacing={3} sx={{ mb: 4 }}>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Total Documents"
            value={stats.documents}
            icon={<DocumentIcon fontSize="large" />}
            color="primary"
            loading={loading}
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Total BOMs"
            value={stats.boms}
            icon={<BOMIcon fontSize="large" />}
            color="secondary"
            loading={loading}
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Open Tasks"
            value={stats.tasks}
            icon={<TaskIcon fontSize="large" />}
            color="warning"
            loading={loading}
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Total Changes"
            value={stats.changes}
            icon={<ChangeIcon fontSize="large" />}
            color="success"
            loading={loading}
          />
        </Grid>
      </Grid>

      {/* Recent Activities */}
      <Grid container spacing={3}>
        <Grid item xs={12} md={8}>
          <Paper sx={{ p: 2 }}>
            <Typography variant="h6" gutterBottom>
              Recent Activities
            </Typography>
            <List>
              {recentActivities.map((activity) => (
                <ListItem key={activity.id} divider>
                  <ListItemIcon>
                    {getActivityIcon(activity.type)}
                  </ListItemIcon>
                  <ListItemText
                    primary={activity.title}
                    secondary={activity.time}
                  />
                  <Chip
                    label={activity.status}
                    color={getStatusColor(activity.status)}
                    size="small"
                  />
                </ListItem>
              ))}
            </List>
          </Paper>
        </Grid>

        <Grid item xs={12} md={4}>
          <Paper sx={{ p: 2 }}>
            <Typography variant="h6" gutterBottom>
              Quick Actions
            </Typography>
            <Box display="flex" flexDirection="column" gap={2}>
              <Card variant="outlined" sx={{ cursor: 'pointer', '&:hover': { bgcolor: 'action.hover' } }}>
                <CardContent>
                  <Typography variant="subtitle1">Upload Document</Typography>
                  <Typography variant="body2" color="textSecondary">
                    Add new design files or specifications
                  </Typography>
                </CardContent>
              </Card>
              <Card variant="outlined" sx={{ cursor: 'pointer', '&:hover': { bgcolor: 'action.hover' } }}>
                <CardContent>
                  <Typography variant="subtitle1">Create BOM</Typography>
                  <Typography variant="body2" color="textSecondary">
                    Start a new bill of materials
                  </Typography>
                </CardContent>
              </Card>
              <Card variant="outlined" sx={{ cursor: 'pointer', '&:hover': { bgcolor: 'action.hover' } }}>
                <CardContent>
                  <Typography variant="subtitle1">Assign Task</Typography>
                  <Typography variant="body2" color="textSecondary">
                    Create and assign new tasks
                  </Typography>
                </CardContent>
              </Card>
            </Box>
          </Paper>
        </Grid>
      </Grid>
    </Box>
  );
}