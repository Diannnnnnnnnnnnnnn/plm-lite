import React, { useState } from "react";
import { ThemeProvider, createTheme } from '@mui/material/styles';
import { CssBaseline } from '@mui/material';
import MainLayout from "./components/Layout/MainLayout";
import Dashboard from "./components/Dashboard/Dashboard";
import DocumentManager from "./components/Documents/DocumentManager";
import BOMManager from "./components/BOM/BOMManager";
import TaskManager from "./components/Tasks/TaskManager";
import ChangeManager from "./components/Changes/ChangeManager";
import UserList from "./components/UserList";
import GlobalSearch from './components/GlobalSearch';
import Auth from "./components/Auth/Auth";

const theme = createTheme({
  palette: {
    primary: {
      main: '#1976d2',
    },
    secondary: {
      main: '#dc004e',
    },
  },
  components: {
    MuiCssBaseline: {
      styleOverrides: {
        body: {
          '&::-webkit-scrollbar': {
            display: 'none'
          },
          scrollbarWidth: 'none',
          overflow: 'hidden'
        },
        html: {
          '&::-webkit-scrollbar': {
            display: 'none'
          },
          scrollbarWidth: 'none',
          overflow: 'hidden'
        }
      }
    }
  }
});

function App() {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [currentPage, setCurrentPage] = useState('Dashboard');

  const renderContent = () => {
    switch (currentPage) {
      case 'Dashboard':
        return <Dashboard />;
      case 'Documents':
        return <DocumentManager />;
      case 'BOMs':
        return <BOMManager />;
      case 'Tasks':
        return <TaskManager />;
      case 'Changes':
        return <ChangeManager />;
      case 'Users':
        return <UserList />;
      case 'Search':
        return <GlobalSearch />;
      default:
        return <Dashboard />;
    }
  };

  if (!isAuthenticated) {
    return (
      <ThemeProvider theme={theme}>
        <CssBaseline />
        <Auth onLogin={() => setIsAuthenticated(true)} />
      </ThemeProvider>
    );
  }

  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <MainLayout currentPage={currentPage} onPageChange={setCurrentPage}>
        {renderContent()}
      </MainLayout>
    </ThemeProvider>
  );
}

export default App;
