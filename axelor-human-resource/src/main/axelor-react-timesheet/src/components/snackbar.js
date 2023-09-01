import React, { Component } from "react";
import "./snackbar.css";
import { Alert } from "react-bootstrap";

class Snackbar extends Component {
  constructor(props) {
    super(props);
    this.state = {
      showSnackbar: this.props.visible || false,
    };
    this.timeout = null;
  }
  componentDidUpdate(prevProps, nextprops) {
    if (prevProps.visible !== this.props.visible) {
      if (this.props.visible) {
        this.showSnackbar();
      } else {
        this.hideSnackbar();
      }
    }
  }
  showSnackbar = () => {
    this.setState({ showSnackbar: true });
    clearTimeout(this.timeout);
    this.timeout = setTimeout(() => {
      this.hideSnackbar();
    }, 1000);
  };

  hideSnackbar = () => {
    this.props.handleAlert();
    this.setState({ showSnackbar: false });
    clearTimeout(this.timeout);
  };

  componentWillUnmount() {
    clearTimeout(this.timeout);
  }

  render() {
    const { message, type } = this.props;
    const { showSnackbar } = this.state;

    return (
      <div>
        {showSnackbar && (
          <Alert bsStyle={type} onDismiss={this.hideSnackbar}>
            <p>{message}</p>
          </Alert>
        )}
      </div>
    );
  }
}

export default Snackbar;
