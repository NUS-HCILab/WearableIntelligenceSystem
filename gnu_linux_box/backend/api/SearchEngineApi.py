from flask import render_template, make_response
from flask_restful import Resource, reqparse, fields, marshal_with
#from db import Database
from datetime import datetime
#from bson.objectid import ObjectId
#from flask_bcrypt import Bcrypt
#from flask_jwt_extended import (
#    JWTManager,
#    jwt_required,
#    create_access_token,
#    get_jwt_identity,
#    set_access_cookies,
#    create_refresh_token,
#)


class SearchEngineApi(Resource):
    def __init__(self, tools):
        self.tools = tools
        pass

    def send_fail(self):
        resp = dict()
        resp["success"] = False
        return resp

    def post(self):  # NOTE that this is actually using username and not userId...
        # define args to accepts from post
        parser = reqparse.RequestParser()
        parser.add_argument("timestamp", type=int)
        parser.add_argument("query", type=str)
        args = parser.parse_args()

        # get incoming text
        timestamp = args["timestamp"]
        query = args["query"]

        #run natural language query
        query_response = self.tools.search_engine(query)

        #build payload
        if query_response is not None:
            resp = dict()
            resp["success"] = True
            resp["response"] = query_response
            print("SER is")
            print(query_response)
        else:
            return self.send_fail()

        return resp
