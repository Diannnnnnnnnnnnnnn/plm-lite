import React from 'react';
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
  Chip
} from '@mui/material';
import {
  Description as DocumentIcon,
  AccountTree as BOMIcon,
  Assignment as TaskIcon,
  TrendingUp as TrendingUpIcon
} from '@mui/icons-material';

const StatCard = ({ title, value, icon, color = 'primary' }) => (
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
        </Box>
        <Box color={`${color}.main`}>
          {icon}
        </Box>
      </Box>
    </CardContent>
  </Card>
);

const recentActivities = [
  { id: 1, type: 'document', title: 'Product Specification v2.1 uploaded', time: '2 hours ago', status: 'new' },
  { id: 2, type: 'bom', title: 'BOM-001 updated by John Doe', time: '4 hours ago', status: 'updated' },
  { id: 3, type: 'task', title: 'Design Review completed', time: '1 day ago', status: 'completed' },
  { id: 4, type: 'document', title: 'Technical Drawing TD-001 approved', time: '2 days ago', status: 'approved' }
];

const getActivityIcon = (type) => {
  switch (type) {
    case 'document': return <DocumentIcon />;
    case 'bom': return <BOMIcon />;
    case 'task': return <TaskIcon />;
    default: return <DocumentIcon />;
  }
};

const getStatusColor = (status) => {
  switch (status) {
    case 'new': return 'primary';
    case 'updated': return 'warning';
    case 'completed': return 'success';
    case 'approved': return 'success';
    default: return 'default';
  }
};

export default function Dashboard() {
  return (
    <Box>
      {/* Statistics Cards */}
      <Grid container spacing={3} sx={{ mb: 4 }}>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Total Documents"
            value="156"
            icon={<DocumentIcon fontSize="large" />}
            color="primary"
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Active BOMs"
            value="23"
            icon={<BOMIcon fontSize="large" />}
            color="secondary"
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Open Tasks"
            value="8"
            icon={<TaskIcon fontSize="large" />}
            color="warning"
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="This Month"
            value="+12%"
            icon={<TrendingUpIcon fontSize="large" />}
            color="success"
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