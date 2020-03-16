module Data.Project exposing (Project, fetchProjects)

import Date exposing (Date)
import Http
import Json.Decode as JD exposing (Decoder, andThen, float, int, string)
import Json.Decode.Extra exposing (fromResult)
import Task exposing (Task)


type alias Project =
    { id : String
    , name : String
    , colour : String
    , company : String
    , abbreviation : String
    , total_time : Float
    , created : Date
    }

--lookupProject : Int -> List Project -> Maybe Project
--lookupProject id projects =
--    List.filter (\p -> p.id == id) projects
--        |> List.head


date : Decoder Date
date =
    string |> andThen (Date.fromString >> fromResult)

projects : Decoder (List Project)
projects =
    let
        project =
            JD.map7 Project
                (JD.field "_id" string)
                (JD.field "name" string)
                (JD.field "colour" string)
                (JD.field "company" string)
                (JD.field "abbreviation" string)
                (JD.field "total_time" float)
                (JD.field "created" date)
    in
        JD.list project


fetchProjects : Task Http.Error (List Project)
fetchProjects =
    Http.get "/api/projects" projects
        |> Http.toTask
