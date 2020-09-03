import React from "react";
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
                   responsePayload : "(nothing yet.)"
                 };

    this.handleOpenAPIURLChange = this.handleOpenAPIURLChange.bind(this);
    this.handleChange = this.handleChange.bind(this);
    this.handleForm = this.handleForm.bind(this);
    this.handleFormChange = this.handleFormChange.bind(this);
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
  <Form schema={this.state.selected.schema} onSubmit={this.handleForm} onChange={this.handleFormChange} formData={this.state.requestPayload} />
</div>
<div className="col">
  <div className="form-group">
    <label htmlFor="exampleFormControlTextarea1">Form request payload:</label>
    <textarea className="form-control" id="exampleFormControlTextarea1" rows="10" value={JSON.stringify(this.state.requestPayload, null, 2)} readOnly></textarea>
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
