import React from "react";
import Button from 'react-bootstrap/Button';
import Modal from 'react-bootstrap/Modal';
import SwaggerClient from "swagger-client";

import { withTheme } from '@rjsf/core';
import { Theme as Bootstrap4Theme } from '@rjsf/bootstrap-4';

class SwaggerResult {
  constructor(endpoints, servers) {
      this.endpoints = endpoints;
      this.servers = servers;
  }
}

const Form = withTheme(Bootstrap4Theme);

async function schemaOptions(openapiURL){
  const a = await SwaggerClient.resolve({url: openapiURL});
  const s = a.spec.servers != null ? a.spec.servers : [];
  let endpoints = [];
  const b = a.spec.paths;
  for (const url in b) {
    try {
      const schema = b[url]["post"]["requestBody"]["content"]["application/json"]["schema"];
      if (schema != null) {
        endpoints.push({url : url, schema : schema});
      }
    } catch (error) {
      // the path url does not define any post for json, compatible with this app.
    }
  }
  const result = new SwaggerResult(endpoints, s);
  return result;
}

class OverrideRequestModal extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      show: false,
      value: JSON.stringify(props.getCurrentRequestValue(), null, 2)
    }
    this.getCurrentRequestValue = props.getCurrentRequestValue;
    this.onSave = props.onSave;
    this.handleShow = this.handleShow.bind(this);
    this.handleClose = this.handleClose.bind(this);
    this.handleChange = this.handleChange.bind(this);
    this.handleSave = this.handleSave.bind(this);
    this.handleApply = this.handleApply.bind(this);
    this.handleReset = this.handleReset.bind(this);
  }

  handleShow() {
    this.setState({value : JSON.stringify(this.getCurrentRequestValue(), null, 2)});
    this.setState({show: true});
  }

  handleClose() {
    this.setState({show: false});
  }

  handleChange(event) {
    this.setState({value: event.target.value});
  }

  handleSave() {
    try {
      console.log(this.state.value);
      var obj = JSON.parse(this.state.value);
      console.log(obj);
      this.onSave(obj);
    } catch (error) {
      console.error("the data manually put in the form is not a valid JSON object.");
    }
    this.setState({show: false});
  }
  handleApply() {
    this.onSave(this.getCurrentRequestValue());
  }
  handleReset() {
    this.onSave({});
  }

  render() {
    return (
      <div>
        <Button variant="light" onClick={this.handleShow}>
          Override request payload manually
        </Button>
        <Button variant="outline-light" onClick={this.handleApply}>
          Apply
        </Button>
        <Button variant="outline-light" onClick={this.handleReset}>
          Reset
        </Button>

        <Modal show={this.state.show} onHide={this.handleClose} animation={false}>
          <Modal.Header closeButton>
            <Modal.Title>Define request payload manually</Modal.Title>
          </Modal.Header>
          <Modal.Body>
            <textarea className="form-control" id="modalTextArea" rows="10" value={this.state.value} onChange={this.handleChange}/>
          </Modal.Body>
          <Modal.Footer>
            <Button variant="secondary" onClick={this.handleClose}>
              Close
            </Button>
            <Button variant="primary" onClick={this.handleSave}>
              Save Changes
            </Button>
          </Modal.Footer>
        </Modal>
      </div>
    );
  }
}

const EMPTY_SELECTED = {url : "loading", schema: {
  "title": "Please enter openapi URL",
  "type": "object"
}};

class MyOpenAPIForm extends React.Component {
  constructor(props) {
    super(props);
    this.myResult = null;
    console.log(this.myResult);
    this.state = { openapiURL : "/openapi",
                   schemas: [],
                   selected : EMPTY_SELECTED,
                   requestPayload : {},
                   responsePayload : "(nothing yet.)",
                   servers: [],
                   selectedServer: "",
                   key: Date.now() // required to reset any possible validation errors
                 };

    this.handleOpenAPIURLChange = this.handleOpenAPIURLChange.bind(this);
    this.handleChange = this.handleChange.bind(this);
    this.handleForm = this.handleForm.bind(this);
    this.handleFormChange = this.handleFormChange.bind(this);
    this.overrideRequest = this.overrideRequest.bind(this);
    this.getCurrentRequestValue = this.getCurrentRequestValue.bind(this);
    this.handleServerChange = this.handleServerChange.bind(this);
    this.refreshSchemasFromOpenapiURL(this.state.openapiURL);
  }

  handleChange(event) {
    this.setState({selected: this.state.schemas.find(x => x.url === event.target.value), requestPayload : {}, responsePayload : "(nothing yet.)"});
  }

  handleOpenAPIURLChange(event) {
    const newURL = event.target.value;
    this.setState({openapiURL: newURL});
    this.refreshSchemasFromOpenapiURL(newURL);
  }

  refreshSchemasFromOpenapiURL(openapiURL) {
    schemaOptions(openapiURL).then(x => this.setState({schemas: x.endpoints,
                                                       selected: x.endpoints?.length > 0 ? x.endpoints[0] : EMPTY_SELECTED,
                                                       servers: x.servers,
                                                       selectedServer : x.servers?.length > 0 ? x.servers[0].url : "",
                                                       requestPayload : {},
                                                       responsePayload : "(nothing yet.)"}),
                                   err => this.setState({schemas: [], selected: { url : "Invalid_openAPI_url", schema: { "title": "Please enter a valid openapi URL", "type": "object" } } }));
  }

  handleFormChange(a) {
    const formData = a.formData;
    this.setState({requestPayload: formData});
  }

  handleServerChange(e) {
    this.setState({selectedServer: e.target.value});
  }

  overrideRequest(x) {
    console.log(x);
    this.setState({
      requestPayload: x,
      key: Date.now() // reset any possible validation errors
    });
  }

  getCurrentRequestValue() {
    return this.state.requestPayload;
  }

  handleForm(a) {
    const formData = a.formData;
    this.setState({requestPayload: formData});
    const other_params = {
      headers: {
        'Accept': 'application/json, text/plain',
        'Content-Type': 'application/json'
    },
      body: JSON.stringify(formData),
      method: "POST",
      mode: "cors"
    };
    const destURL = this.state.selectedServer + this.state.selected.url;
    fetch(destURL, other_params)
    .then(function(response) {
      return response.json();
    }).then((data) => {
      console.log(data);
      this.setState({responsePayload: data});
    });
  }

  render() {
    return (
<div>

<div className="row">
<div className="col">
  <div className="form-group">
    <label htmlFor="openapiURLInput">OpenAPI URL:</label>
    <input type="text" className="form-control" id="openapiURLInput" value={this.state.openapiURL} onChange={this.handleOpenAPIURLChange} />
  </div>
</div>
<div className="col">
  <div className="form-group">
    <label htmlFor="exampleFormControlSelect1">Select POST endpoint:</label>
    <select className="form-control" id="exampleFormControlSelect1" value={this.state.selected.url} onChange={this.handleChange}>
        {
        this.state.schemas.map(s => (
          <option key={s.url} value={s.url} > {s.url} </option>
        ))
        }
    </select>
  </div>
  <div className="form-group row" style={this.state.servers?.length > 0 ? {} : { display: 'none' }}>
    <label htmlFor="serverFormControlSelect" className="col-sm-2 col-form-label">Server:</label>
    <div className="col-sm-10">
      <select className="form-control" id="serverFormControlSelect" value={this.state.selectedServer} onChange={this.handleServerChange} >
        {
        this.state.servers?.map(s => (
          <option key={s.url} value={s.url} >{s.url}</option>
        ))
        }
      </select>
    </div>
  </div>
</div>
</div>

<hr />

<div className="row">
<div className="col">
  <Form key={this.state.key} schema={this.state.selected.schema} onSubmit={this.handleForm} onChange={this.handleFormChange} formData={this.state.requestPayload} noHtml5Validate={true} />
</div>
<div className="col">
  <div className="form-group">
    <label htmlFor="exampleFormControlTextarea1">Form request payload:</label>
    <textarea className="form-control" id="exampleFormControlTextarea1" rows="10" value={JSON.stringify(this.state.requestPayload, null, 2)} readOnly></textarea>
    <OverrideRequestModal getCurrentRequestValue={this.getCurrentRequestValue} onSave={this.overrideRequest}/>
  </div>
  <div className="form-group">
    <label htmlFor="exampleFormControlTextarea2">Response:</label>
    <textarea className="form-control" id="exampleFormControlTextarea2" rows="22" value={JSON.stringify(this.state.responsePayload, null, 2)} readOnly></textarea>
  </div>
</div>
</div>

</div>
    );
  }
}

export default MyOpenAPIForm;
