const path = require('path');
const _ = require('lodash');

const ExtractCssChunks = require('extract-css-chunks-webpack-plugin');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const CopyWebpackPlugin = require('copy-webpack-plugin');

const {
  CleanWebpackPlugin
} = require('clean-webpack-plugin');

const devServerPort = 3000;

const devServer = {
  hot: true,
  disableHostCheck: true,
  clientLogLevel: 'none',
  open: true,
  public: 'http://localhost:3000',
  port: devServerPort,
  historyApiFallback: {
    index: ''
  }
};


function common(variables, mode) {
    const postcssOptions = {
    postcssOptions: {
      path: path.resolve(
        __dirname,
        (mode === 'production') ?
        './postcss.prod.config.js' :
        './postcss.config.js'
      )
    }
    };


  return {
    mode: mode,
    resolve: {
      modules: [
        "node_modules",
        path.resolve(__dirname, "node_modules"),
        path.resolve(__dirname, "modules/frontend/vendor/"),
      ],
    },
    output: {
      publicPath: '/',
      filename: '[name].[hash].js',
      library: 'app',
      libraryTarget: 'var'
    },
    entry: [
      path.resolve(__dirname, './example/src/main/resources/main.scss')
    ],
    module: {
      rules: [{
          test: /\.js$/,
          enforce: 'pre',
          use: [{
            loader: 'scalajs-friendly-source-map-loader',
            options: {
              name: '[name].[contenthash:8].[ext]',
              bundleHttp: false
            }
          }]
        },
                {
                  test: /\.css$/,
                  use: [{
                      loader: ExtractCssChunks.loader,
                      options: {
                        filename: '[name].[contenthash:8].[ext]'
                      }
                    },
                    {
                      loader: 'css-loader'
                    },
                    {
                      loader: "postcss-loader",
                      options: postcssOptions
                    }
                  ]
                },
                {
                  test: /\.scss$/,
                  use: [
                  "style-loader",
                  "css-loader",
                  "sass-loader",
//                  {
//                      loader: ExtractCssChunks.loader,
//                      options: {
//                        filename: '[name].[contenthash:8].[ext]'
//                      }
//                    },
//                    {
//                      loader: 'css-loader'
//                    },
////                    {
////                      loader: "postcss-loader",
////                      options: postcssOptions
////                    },
//                    {
//                      loader: 'sass-loader'
//                    }
                  ]
                }
      ]
    },
    plugins: [
      new HtmlWebpackPlugin({
        filename: 'index.html',
        template: './example/src/main/resources/index-fastopt.html',
        minify: false,
        inject: 'head',
        config: variables
      }),
    ],
    devServer
  }
}

const dev = {
  entry: [
    path.resolve(
      __dirname,
      "./example/target/scala-2.13/example-fastopt/main.js"
    )
  ]
};

const prod = {
  entry: [
    path.resolve(__dirname, './example/target/scala-2.13/example-opt/main.js'),
  ],
  plugins: [
    new CleanWebpackPlugin(),
  ]
};


function customizer(objValue, srcValue) {
  if (_.isArray(objValue)) {
    return objValue.concat(srcValue);
  }
}

module.exports = function (env) {
  switch (process.env.npm_lifecycle_event) {
    case 'build:prod':
      return _.mergeWith({}, common({}, 'production'), prod, customizer);

    default:
      console.log('using dev config');
      return _.mergeWith({}, common({}, 'development'), dev, customizer);
  }
};
