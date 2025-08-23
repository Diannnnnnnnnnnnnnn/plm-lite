import React from "react";
import TaskList from "./components/TaskList";
import UserList from "./components/UserList";
import AddTask from "./components/AddTask";
import TaskSearch from './components/TaskSearch';

function App() {
  return (
    <div>
      <h1>Task Management App</h1>
      <AddTask />
      <TaskList />
      <UserList />
      <TaskSearch />
    </div>
  );
}

export default App;
