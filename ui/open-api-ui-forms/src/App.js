import React from "react";
import Button from 'react-bootstrap/Button';
import Modal from 'react-bootstrap/Modal';
import SwaggerClient from "swagger-client";

import { withTheme } from '@rjsf/core';
import { Theme as Bootstrap4Theme } from '@rjsf/bootstrap-4';

const Form = withTheme(Bootstrap4Theme);

async function schemaOptions(openapiURL){
  const a = await new SwaggerClient(openapiURL);
  let res = [];
  const b = a.spec.paths;
  for (const url in b) {
    const schema = b[url]["post"]["requestBody"]["content"]["application/json"]["schema"];
    if (schema != null) {
      res.push({url : url, schema : schema});
    }
  }
  return res;    
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

class MyOpenAPIForm extends React.Component {
  constructor(props) {
    super(props);
    this.myResult = null;
    console.log(this.myResult);
    this.state = { openapiURL : "/openapi",
                   schemas: [],
                   selected :  {url : "loading", schema: {
  "title": "Please enter openapi URL",
  "type": "object"
}},
                   requestPayload : {},
                   responsePayload : "(nothing yet.)",
                   key: Date.now() // required to reset any possible validation errors
                 };

    this.handleOpenAPIURLChange = this.handleOpenAPIURLChange.bind(this);
    this.handleChange = this.handleChange.bind(this);
    this.handleForm = this.handleForm.bind(this);
    this.handleFormChange = this.handleFormChange.bind(this);
    this.overrideRequest = this.overrideRequest.bind(this);
    this.getCurrentRequestValue = this.getCurrentRequestValue.bind(this);
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
    schemaOptions(openapiURL).then(x => this.setState({schemas: x, selected: x[0], requestPayload : {}, responsePayload : "(nothing yet.)"}),
                                   err => this.setState({schemas: [], selected: { url : "Invalid_openAPI_url", schema: { "title": "Please enter a valid openapi URL", "type": "object" } } }));
  }

  handleFormChange(a) {
    const formData = a.formData;
    this.setState({requestPayload: formData});
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
    fetch(this.state.selected.url, other_params)
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
 {this.state.schemas.map(s => (
            <option
              key={s.url}
              value={s.url}
            >
              {s.url}
            </option>
          ))}
          </select>
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
