module Data.TimeLog exposing (TimeLog, fetchTimeLogs)

import Date exposing (Date)
import Http
import Json.Decode as JD exposing (Decoder, andThen, fail, float, int, string, succeed)
import Json.Decode.Extra exposing (fromResult)
import Task exposing (Task)


type alias TimeLog =
    { id : String
    , description : String
    , time : Float
    , log_date : Date
    , project_id : String
    , created : Date
    }


--lookupTimeLog : Int -> List TimeLog -> Maybe TimeLog
--lookupTimeLog id timeLogs =
--    List.filter (\p -> p.id == id) timeLogs
--        |> List.head

date : Decoder Date
date =
    string |> andThen (Date.fromString >> fromResult)


timeLogs : Decoder (List TimeLog)
timeLogs =
    let
        timeLog =
            JD.map6 TimeLog
                (JD.field "_id" string)
                (JD.field "description" string)
                (JD.field "time" float)
                (JD.field "log_date"  date)
                (JD.field "project_id" string)
                (JD.field "created" date)
    in
        JD.list timeLog


fetchTimeLogs : Task Http.Error (List TimeLog)
fetchTimeLogs =
    Http.get "/api/tasks" timeLogs
        |> Http.toTask
