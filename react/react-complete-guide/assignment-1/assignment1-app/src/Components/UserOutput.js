import React, { Component } from 'react';

class UserOutput extends Component {
    render() {
        return (
            <div>
                <h2>{this.props.username}</h2>
                <p>paragraph 1</p>
                <p>paragraph 2</p>
            </div>
        )
    };
}

export default UserOutput;