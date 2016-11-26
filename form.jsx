var Badge = require('badge');
var Autocompleter = require('autocompleter');

// render function, sometime later

render: function() {
  return <div>
    <Autocompleter list={this.props.list} />
    <Badge text={this.prop.text} number={this.props.number} />
  </div>
}
