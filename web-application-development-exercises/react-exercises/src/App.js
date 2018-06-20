import React, { Component } from 'react';
import logo from './logo.svg';
import './App.css';
import { Ex7Component } from './exercises/Ex7ComponentWD';
import {Ex17Component} from './exercises/Ex17Component';
const statusForAll = require('./exercises/Ex15StatusForAll').statusForAll;

class App extends Component {
  render() {
    return (
      <div className="App">
        <header className="App-header">
          <img src={logo} className="App-logo" alt="logo" />
          <h1 className="App-title">Welcome to React</h1>
        </header>
        <p className="App-intro">
          To get started, edit <code>src/App.js</code> and save to reload.
        </p>
        {testEx17Component()}
      </div>
    );
  }
}

function testEx7Component(){
    const cb = (statusCode) => console.log('Callback Status Code : ' + statusCode);
    const url = 'http://api.github.com';
    const time = 1000;
    return <Ex7Component url={url} timeInterval={time} callback={cb} />;
}

function testEx15StatusForAll(){
    const urls = ['http://api.github.com', 'http://', 'http://api.github.com']
    statusForAll(urls).then(codes => {
        for(let i = 0; i < codes.length; i++)
            console.log(codes[i]);
    }).catch(err => console.log(err));        
}

function testEx17Component(){
    const url = 'http://api.github.com';
    return <Ex17Component url={url}/>;
}

export default App;
