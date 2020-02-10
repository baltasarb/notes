import React, { Component } from 'react';
import './App.css';
import UserInput from './Components/UserInput';
import UserOutput from './Components/UserOutput';

class App extends Component {
  state = {
    username: "username1"
  };

  changeUsernameHandler = (event) => {
    console.log("test")
    this.setState({
      username: event.target.value
    })
   
  };

  render() {
    return (
      <div className="App">
        <UserInput username={this.state.username} usernameChangeHandler={this.changeUsernameHandler}/>
        <UserOutput username={this.state.username} />
      </div>
    );
  }
}

export default App;
