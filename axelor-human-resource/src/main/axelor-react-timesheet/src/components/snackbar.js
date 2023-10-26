/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
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
