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
import Settings from './components/Settings/Settings';
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
  const [currentUser, setCurrentUser] = useState(null);

  // Check if user is already logged in on mount
  React.useEffect(() => {
    const loadUser = () => {
      const storedUser = localStorage.getItem('user');
      if (storedUser) {
        try {
          const userData = JSON.parse(storedUser);
          setCurrentUser(userData);
          setIsAuthenticated(true);
        } catch (error) {
          console.error('Error parsing stored user data:', error);
          localStorage.removeItem('user');
        }
      }
    };

    loadUser();

    // Listen for storage changes (when user profile is updated in Settings)
    const handleStorageChange = () => {
      loadUser();
    };

    window.addEventListener('storage', handleStorageChange);

    return () => {
      window.removeEventListener('storage', handleStorageChange);
    };
  }, []);

  const handleLogin = () => {
    const storedUser = localStorage.getItem('user');
    if (storedUser) {
      setCurrentUser(JSON.parse(storedUser));
    }
    setIsAuthenticated(true);
  };

  const handleLogout = () => {
    localStorage.removeItem('user');
    setCurrentUser(null);
    setIsAuthenticated(false);
    setCurrentPage('Dashboard');
  };

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
      case 'Settings':
        return <Settings />;
      default:
        return <Dashboard />;
    }
  };

  if (!isAuthenticated) {
    return (
      <ThemeProvider theme={theme}>
        <CssBaseline />
        <Auth onLogin={handleLogin} />
      </ThemeProvider>
    );
  }

  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <MainLayout
        currentPage={currentPage}
        onPageChange={setCurrentPage}
        currentUser={currentUser}
        onLogout={handleLogout}
      >
        {renderContent()}
      </MainLayout>
    </ThemeProvider>
  );
}

export default App;
