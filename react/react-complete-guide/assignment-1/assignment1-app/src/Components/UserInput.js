import React, { Component } from 'react';

class UserInput extends Component {
    render() {
        return (
            <div>
                <input 
                    type="text" 
                    onChange={this.props.usernameChangeHandler} 
                    value={this.props.username}
                />
            </div>
        )
    };
}

export default UserInput;