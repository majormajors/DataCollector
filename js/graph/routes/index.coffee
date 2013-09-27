mongoose = require("mongoose")
Jump = mongoose.model("Jump")

requirejs = require("requirejs")
requirejs.config(
  nodeRequire: require
)

#
# * GET home page.
# 

altCoef = 1.0 / 5.255

sumPressureData = (barometer) ->
  data = []
  data.push(x: v[0] / 1000.0, y: (v[1] + v[2] + v[3]) / 100.0) for v in barometer
  data

calculateAltitudeChanged = (p0, p) ->
  (44330.0 * (1.0 - Math.pow(p/p0, altCoef))) * 3.2808 / 1000.0

calculateAltitudeData = (barometer) ->
  initialBarometerReading = barometer[0][1]
  data = []
  data.push(x: v[0] / 1000.0, y: calculateAltitudeChanged(initialBarometerReading, v[1])) for v in barometer
  data

exports.index = (req, res) ->
  query = Jump.find {}
  query.exec (err, jumps) ->
    jump = jumps[0]

    res.render "index",
      title: "Jump Graph"
      pressure_data: sumPressureData(jump.barometer)
      altitude_data: calculateAltitudeData(jump.barometer)
