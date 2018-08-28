// @flow
import React from "react";
import type { Permission } from "../types/Permissions";
import { Checkbox } from "../../components/forms/index";
import { DeleteButton } from "../../components/buttons/index";
import { translate } from "react-i18next";
import { Select } from "../../components/forms/index";
import {
  modifyPermission,
  isModifyPermissionPending,
  getModifyPermissionFailure,
  modifyPermissionReset
} from "../modules/permissions";
import connect from "react-redux/es/connect/connect";
import { withRouter } from "react-router-dom";
import type { History } from "history";
import ErrorNotification from "../../components/ErrorNotification";

type Props = {
  submitForm: Permission => void,
  modifyPermission: (Permission, string, string) => void,
  permission: Permission,
  t: string => string,
  namespace: string,
  name: string,
  match: any,
  history: History,
  loading: boolean,
  error: Error,
  permissionReset: (string, string, string) => void
};

type State = {
  permission: Permission
};

class SinglePermission extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);

    this.state = {
      permission: {
        name: "",
        type: "READ",
        groupPermission: false,
        _links: {}
      }
    };
  }

  componentDidMount() {
    const { permission } = this.props;
    this.props.permissionReset(
      this.props.namespace,
      this.props.name,
      permission.name
    );
    if (permission) {
      this.setState({
        permission: {
          name: permission.name,
          type: permission.type,
          groupPermission: permission.groupPermission,
          _links: permission._links
        }
      });
    }
  }

  render() {
    const { permission } = this.state;
    const { t, loading, error } = this.props;
    const types = ["READ", "OWNER", "GROUP"];
    const deleteButton = this.props.permission._links.delete ? (
      <DeleteButton label={t("edit-permission.delete-button")} />
    ) : null;

    const typeSelector = this.props.permission._links.update ? (
      <td>
        <Select
          onChange={this.handleTypeChange}
          value={permission.type ? permission.type : ""}
          options={this.createSelectOptions(types)}
          loading={loading}
        />
      </td>
    ) : (
      <td>{permission.type}</td>
    );

    const errorNotification = error ? (
      <ErrorNotification error={error} />
    ) : null;

    return (
      <tr>
        <td>{permission.name}</td>
        <td>
          <Checkbox checked={permission ? permission.groupPermission : false} />
        </td>
        {typeSelector}
        <td>{deleteButton}</td> {errorNotification}
      </tr>
    );
  }

  handleTypeChange = (type: string) => {
    this.setState({
      permission: {
        ...this.state.permission,
        type: type
      }
    });
    this.modifyPermission(type);
  };

  modifyPermission = (type: string) => {
    let permission = this.state.permission;
    permission.type = type;
    this.props.modifyPermission(
      permission,
      this.props.namespace,
      this.props.name
    );
  };

  createSelectOptions(types: string[]) {
    return types.map(type => {
      return {
        label: type,
        value: type
      };
    });
  }
}

const mapStateToProps = (state, ownProps) => {
  const permission = ownProps.permission;
  const loading = isModifyPermissionPending(
    state,
    ownProps.namespace,
    ownProps.name,
    permission.name
  );
  const error = getModifyPermissionFailure(
    state,
    ownProps.namespace,
    ownProps.name,
    permission.name
  );

  return { loading, error };
};

const mapDispatchToProps = dispatch => {
  return {
    modifyPermission: (
      permission: Permission,
      namespace: string,
      name: string
    ) => {
      dispatch(modifyPermission(permission, namespace, name));
    },
    permissionReset: (
      namespace: string,
      name: string,
      permissionname: string
    ) => {
      dispatch(modifyPermissionReset(namespace, name, permissionname));
    }
  };
};
export default connect(
  mapStateToProps,
  mapDispatchToProps
)(translate("permissions")(withRouter(SinglePermission)));
