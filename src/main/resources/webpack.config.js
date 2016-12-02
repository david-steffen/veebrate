// webpack.config.js
const webpack = require('webpack');
const ExtractTextPlugin = require("extract-text-webpack-plugin");

const static_path = './webroot';

module.exports = {
    devtool: 'source-map',
    entry: './app-client.js',
    output: {
        path: static_path + '/dist',
        filename: 'bundle.js',
        publicPath: '/dist/'
    },
    module: {
        loaders: [
            {
                test: /\.js$/,
                loader: ['babel'],
                query: {
                    presets: ['es2015']
                },
                exclude: /node_modules/
            },{
                test: /\.scss$/,
                exclude: /node_modules/,
                loader: ExtractTextPlugin.extract(['css','sass'])
            }
        ]
    },
    plugins: [
        new webpack.optimize.UglifyJsPlugin({
            compress: {
              warnings: true,
            },
            output: {
              comments: false,
            },
        }),
        new ExtractTextPlugin( '../css/main.css', {
            allChunks: true
        })
    ],
};
