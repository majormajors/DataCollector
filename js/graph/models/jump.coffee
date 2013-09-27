mongoose = require("mongoose")

jumpSchema = mongoose.Schema
  startTime: Date,
  endTime: Date,
  barometer: Array,
  accelerometer: Array,
  humidity: Array,
  magnetometer: Array,
  gyroscope: Array,
  temperature: Array

mongoose.model("Jump", jumpSchema)
