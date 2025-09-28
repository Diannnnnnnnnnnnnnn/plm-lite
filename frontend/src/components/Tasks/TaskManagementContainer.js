import React, { useState } from 'react';
import {
  Box,
  Tabs,
  Tab,
  Paper
} from '@mui/material';
import {
  Assignment as TaskIcon,
  Analytics as AnalyticsIcon,
  Dashboard as DashboardIcon
} from '@mui/icons-material';

import TaskManager from './TaskManager';
import TaskAnalytics from './TaskAnalytics';
import TaskDetails from './TaskDetails';

const TaskManagementContainer = () => {
  const [currentTab, setCurrentTab] = useState(0);
  const [selectedTask, setSelectedTask] = useState(null);
  const [taskDetailsOpen, setTaskDetailsOpen] = useState(false);

  const handleTabChange = (event, newValue) => {
    setCurrentTab(newValue);
  };

  const handleTaskSelect = (task) => {
    setSelectedTask(task);
    setTaskDetailsOpen(true);
  };

  const handleTaskUpdate = (updatedTask) => {
    // Handle task update logic here
    console.log('Task updated:', updatedTask);
    setTaskDetailsOpen(false);
  };

  const renderTabContent = () => {
    switch (currentTab) {
      case 0:
        return <TaskManager onTaskSelect={handleTaskSelect} />;
      case 1:
        return <TaskAnalytics />;
      default:
        return <TaskManager onTaskSelect={handleTaskSelect} />;
    }
  };

  return (
    <Box>
      {/* Tab Navigation */}
      <Paper sx={{ mb: 3 }}>
        <Tabs
          value={currentTab}
          onChange={handleTabChange}
          indicatorColor="primary"
          textColor="primary"
        >
          <Tab
            icon={<TaskIcon />}
            label="Task Management"
            iconPosition="start"
          />
          <Tab
            icon={<AnalyticsIcon />}
            label="Analytics"
            iconPosition="start"
          />
        </Tabs>
      </Paper>

      {/* Tab Content */}
      {renderTabContent()}

      {/* Task Details Dialog */}
      <TaskDetails
        task={selectedTask}
        open={taskDetailsOpen}
        onClose={() => setTaskDetailsOpen(false)}
        onUpdate={handleTaskUpdate}
      />
    </Box>
  );
};

export default TaskManagementContainer;